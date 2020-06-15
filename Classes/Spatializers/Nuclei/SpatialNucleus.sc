SpatialNucleus : Hybrid{
	var freeFunctions, canPlay = false;
	var <group, <inputBus, <outputBus, <synth;
	var <lag, <azimuth, <elevation, <distance;
	var pausingRoutine, cilium;

	initHybrid { 
		this.makeBusses; 
		this.makeNodes;
	}

	makeBusses { 
		inputBus ?? {Bus.audio(server, 1)};
		outputBus ?? {outputBus  =  0};
	}

	makeNodes { 
		canPlay = false; 
		this.initGroup;
		this.initSynth;
		canPlay = true;
	}

	initGroup { group ?? {group = Group.new.register} }

	initSynth {
		lag = lag ?? {server.latency  * 0.1};
		synth = Synth(modules.synthDef.name, [ 
			\in, inputBus, 
			\out, outputBus, 
			\angle, pi/2,
			\lag, lag, 
			\timer, server.latency, 
			\doneAction, 0
		], group).register;
		synth.onFree({this.freeList});
	}

	freeList { 
		this.onFree;
		freeFunctions.do(_.value);
	}

	onFree { | function |
		freeFunctions ?? { 
			var list = List.new; 
			list.add({this.freeResources});
			freeFunctions = list;
		}; 
		function !? {freeFunctions.add(function)};
	}

	outputBus_{|newBus|
		outputBus = newBus;
		if(this.isRunning, {synth.set(\out, outputBus)});
	}

	isRunning { synth !? {^synth.isRunning} ?? {^false} }

	isPlaying { ^synth.isPlaying }

	makeTemplates { 
		templater.nucleusShell( "nucleusShell" );
	}

	/**pr_DefineSynthDefShell{ |toWrap|
		var synthdef;
		if(toWrap.class!=Function){
			Error("Input must be a function that returns another function").throw;
		};
		synthdef = SynthDef(\Synth, {
			var timer = \timer.kr(8);
			var env = EnvGen.kr(Env.asr(0.0, 1, \release.kr(1.0)),
				\gate.kr(1),
				doneAction: Done.freeSelf
			);
			var lag = \lag.kr(0.1);
			var in = In.ar(\in.kr(0), 1) * \ampDB.kr(0).dbamp;
			var sig = SynthDef.wrap(toWrap.value(in, lag));
			var wakeUpSignal = EnvGen.kr(Env.perc(0.0, timer/2),
				gate: \wakeUp.tr(1)) * PinkNoise.ar();
			DetectSilence.ar(in+wakeUpSignal, time: timer/2,
				doneAction: \doneAction.kr(1));
			Out.ar(\out.kr(0), sig * env);
		});

		^synthdef;
	}*/

	freeResources { 
		group !? {group.free}; 
		this.freeBus(inputBus); 
		this.freeBus(outputBus);
		inputBus = outputBus = nil;
	}

	freeBus { | bus |
		if(bus.isKindOf(Bus) and: {bus.index.notNil}, { 
			bus.free; 
		});	
	}

	free {
		//I need to think about this more...
		//Awaken only works if this.isRunning is false.
		//However, here, we invoke awaken if the synth is playing and running. This makes no sense.
		case
		{this.isPlaying and: {this.isRunning}}{this.awaken(2)}
		{this.isPlaying and: {this.isRunning.not}}{synth.free;}
		{this.isPlaying.not}{this.freeList};
	}

	setArg { | key, value |
		if(canPlay and: {this.isPlaying}, { 
			synth.set(key, newArg);
		});
	}

	azimuth_{ | newAzimuth(pi) | 
		azimuth = newAzimuth.wrap(pi.neg, pi); 
		this.setArg(\azimuth, azimuth);
	}

	elevation_{ | newElevation(0) | 
		elevation = newElevation.wrap(pi.neg, pi); 
		this.setArg(\elevation, elevation);
	}

	distance_{ | newDistance(2) | 
		distance = newDistance.clip(1.0, 100.0);
		this.setArg(\distance, distance);
	}

	lag_{ |newLag(server.latency)| 
		lag = newLag;
		this.setArg(\lag, lag);
	}

	awaken { | doneAction(0) |
		if(this.isRunning.not, {  
			if(pausingRoutine.isPlaying, {			
				pausingRoutine.stop;
			});
			pausingRoutine = Routine({
				synth.set(\doneAction, 0, \wakeUp, 1);
				synth.run(true);
				(server.latency * 2).wait;
				synth.set(\doneAction, doneAction);
			});
			pausingRoutine.play;
		});
	}

	play{ if(canPlay, {this.awaken}) }

	*loadSpatialCellSynthDefs{
		// Routine({
		var synthArray = this.subclasses.do{|subclass|
			var synthDefs = [];
			synthDefDictionary[subclass.asSymbol].do{|synthDef|
				synthDefs = synthDefs.add(synthDef);
			};
			this.pr_GarbageCollect(synthDefs);
			subclass.initNew;
		};
	}
}
