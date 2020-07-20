SpatialNucleus : Hybrid {
	var freeFunctions;
	var <group, <inputBus, <outputBus, <synth;
	var <lag, <azimuth, <elevation, <distance;
	var pausingRoutine, cilium;

	*makeTemplates { | templater |
		templater.nucleusShell;
		this.setNucleusFunction;
	}
	
	setNucleusFunction { this.subclassResponsibility(thisMethod); }

	setNucleusFunction { | templater | 
		this.subclassResponsibility(thisMethod); 
	}

	makeSynthDefs {
		var synthDef = modules.nucleusShell(modules[\nucleusFunction]);
		synthDef.name = this.formatName(synthDef.name).asSymbol; 
		if(this.checkDictionary(synthDef), { 
			this.class.processSynthDefs(synthDef); 
			modules.add(\synthDef -> synthDef);
		});
	}

	initResources {
		this.makeBusses;
		this.initGroup;
	}

	makeBusses {
		inputBus ?? {Bus.audio(server, 1)};
		outputBus ?? {outputBus  =  0};
	}

	initGroup { group ?? {group = Group.new.register} }

	initSynth {
		lag = lag ?? {server.latency  * 0.1};
		synth = Synth.newPaused(modules.synthDef.name, [
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
		case
		{this.isPlaying and: {this.isRunning}}{synth.set(\doneAction, 2, \gate, 0);}
		{this.isPlaying and: {this.isRunning.not}}{synth.free;}
		{this.isPlaying.not}{this.freeList};
	}

	setArg { | key, value |
		if({this.isPlaying}, { 
			synth.set(key, value);
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

	play{
		if(synth.isNil, { this.initSynth });
		this.awaken
	}
}

MonoNucleus : SpatialNucleus { setNucleusFunction { templater.monoNucleus; } }

StereoNucleus : SpatialNucleus { setNucleusFunction { templater.stereoNucleus; } }

QuadNucleus : SpatialNucleus { setNucleusFunction { templater.quadNucleus; } }

FOANucleus : SpatialNucleus { setNucleusFunction { templater.foaNucleus; } }

HOANucleus : SpatialNucleus { setNucleusFunction { templater.hoaNucleus; } }
