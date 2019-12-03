VROrganismSystem : VROrganismSystemConfigurer{
	//the base class deals with configuration files and setthing them up and stuff
	//this class, the derived class, should set up the oscdefs for the VR and
	//provide a flexible interface for writing funcitons or those oscdefs

	classvar server;
	classvar <deleteFiles = false;
	classvar <spatializerClass;
	classvar <oscAddresses;

	classvar factory, <isRunning = false;
	classvar quitRegistered = false;

	classvar oscSpawn, oscPosition, oscKill, oscSound;
	classvar oscAwake, oscEnabled, oscDisabled, oscMate, oscEat, oscArray;
	// classvar buffers, behaviors;

	*start{|bufferArray, behaviorArray|

		server = server ? Server.default;

		if(server.hasBooted==false){
			Error("Server has not booted!").throw;
		};

		if(isRunning){
			this.free;
		};

		if(quitRegistered==false){
			ServerQuit.add({
				try{this.free;};
			});
			quitRegistered = true;
		};

		SpaceCell.loadSpaceCellSynthDefs;

		super.start;

		/*		behaviorArray = behaviorArray ?? {
		Array.fill(bufferArray.size, {
		VROBehavior.new;
		});
		};*/

		if(bufferArray.isNil){
			Error("System requires an array of buffers.").throw;
		};

		factory = GenerativeOrganismFactory.new(bufferArray, behaviorArray);
	}

	*restart{
		this.start(factory.buffers, factory.behaviors);
	}

	*factory{
		if(isRunning==false or: {factory.isNil}){
			"Warning: the system is not running so there is no factory".postln;
		};

		^factory;
	}

	*buffers_{ |bufferArray|
		factory.buffers = bufferArray;
	}

	*buffers{
		^factory.buffers;
	}

	*behaviors_{
		|behaviorArray|
		factory.behaviors = behaviorArray;
	}

	*behaviors{
		^factory.behaviors;
	}

	*spatializerClass_{|newSpatializerClass|
		if(newSpatializerClass.isVRSpatializer){
			GenerativeOrganismFactory.spatializerClass = newSpatializerClass;
			spatializerClass = newSpatializerClass;
		}/*ELSE*/{
			Error("Can only add spatializers that inherent from VRSpatializer.").throw;
		}
	}

	*deleteFiles_{|bool = false|
		GenerativeOrganismFactory.deleteFiles = bool;
		deleteFiles = bool;
	}

	*pr_SetupVROSCDefs{|addressDictionary|

		oscSpawn = OSCdef(\oscSpawn, {
			|msg|
			var index, bufferIndex, behaviorIndex;

			msg.postln;

			if(msg.size > 1){
				index = msg[1];
			};

			if(msg.size > 2){
				bufferIndex = msg[2];
			}/*ELSE*/{
				if(factory.buffers.size < index){
					bufferIndex = index;
				}/*ELSE*/{
					bufferIndex = (factory.buffers.size - 1).rand;
				}
			};

			if(msg.size > 3){
				behaviorIndex = msg[3];
			}/*ELSE*/{
				if(factory.behaviors.size < index){
					behaviorIndex = index;
				}/*ELSE*/{
					behaviorIndex = (factory.behaviors.size - 1).rand;
				}
			};

			index = index.asInteger.asSymbol;

			factory.spawn(index, bufferIndex , behaviorIndex);

		}, addressDictionary["spawn"]);

		oscPosition = OSCdef(\oscPosition, {
			|msg|
			var index = msg[1];
			var angle = msg[2];
			var elevation = msg[3];
			var distance = msg[4];

			// [\particles, index, \angle, angle, \elevation, elevation, \distance, distance].postln;

			factory.position(index.asSymbol,
				angle, elevation, distance);

		}, addressDictionary["position"]);

		oscKill = OSCdef(\oscKill, {
			|msg|

			var index = msg[1];

			// msg.postln;

			index = index.asInteger.asSymbol;
			factory.kill(index);

		}, addressDictionary["kill"]);

		oscSound = OSCdef(\oscSound, {
			|msg|
			var index = msg[1];
			var typeIndex = msg[2];

			index = index.asInteger.asSymbol;
			factory.playVROrganismFromFactory(index, typeIndex ? 3, 12);

		}, addressDictionary["sound"]);

		oscMate = OSCdef(\oscMate, {
			|msg|
			var partner0 = msg[1].asInteger.asSymbol;
			var partner1 = msg[2].asInteger.asSymbol, newIndex;

			// msg.postln;

			if(msg.size >= 4){
				newIndex = msg[3].asInteger.asSymbol;
			};

			factory.mateOrganisms(partner0, partner1, newIndex);

		}, addressDictionary["mate"]);

		oscEat = OSCdef(\oscEat, {
			|msg|
			var predator = msg[1].asInteger.asSymbol;
			var prey = msg[2].asInteger.asSymbol;

			// msg.postln;

			format("Predator % eats Prey %", predator, prey).postln;

			factory.eatOrganisms(predator, prey);

		}, addressDictionary["eat"]);

		isRunning = true;
		oscAddresses = addressDictionary;

		oscArray = [oscSpawn, oscPosition, oscKill,
			oscSound, oscMate, oscEat];

		CmdPeriod.doOnce({
			if(isRunning){
				isRunning = false;
			};
		});

	}

	*mate{|partner0Index, partner1Index, newIndex|
		if(isRunning){

			NetAddr.localAddr.sendMsg(
				oscAddresses["mate"],
				partner0Index, partner1Index,
				newIndex ? factory.particles.size
			);

		}
	}

	*eat{|predatorIndex, preyIndex|
		if(isRunning){

			NetAddr.localAddr.sendMsg(
				oscAddresses["eat"],
				predatorIndex, preyIndex
			);

		}
	}

	*spawn{|toSpawn, bufferIndex, behaviorIndex|
		if(isRunning){

			NetAddr.localAddr.sendMsg(oscAddresses["spawn"],
				toSpawn, bufferIndex, behaviorIndex);

		};
	}

	*position{|toPosition, angle = 0, elevation = 0, distance = 1|
		if(isRunning){
			NetAddr.localAddr.sendMsg(oscAddresses["position"],
				toPosition, angle, elevation, distance);
		}
	}

	*sound{|toSound, typeIndex|
		if(isRunning){
			NetAddr.localAddr
			.sendMsg(oscAddresses["sound"], toSound, typeIndex);
		}
	}

	*kill{|toKill|
		if(isRunning){
			NetAddr.localAddr.sendMsg(oscAddresses["kill"], toKill);
		}
	}

	*free{
		factory.free;
		oscArray.do{|item|
			item.remove;
		};
		isRunning = false;
	}

}