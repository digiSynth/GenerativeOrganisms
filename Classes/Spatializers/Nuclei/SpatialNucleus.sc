SpatialNucleus : Hybrid{
	classvar <spatializersInit = false;

	var freeFunc;
	var <group, <inputBus, <outputBus, <synth;
	var <event = nil, <function = nil;
	var canPlay = false, <isFreed = false;
	var pausingRoutine = nil;
	var <>distanceMaxFreq = 16000, doneAction = 1;
	var <lag, <azimuth, <elevation, <distance;

	var <mover;

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
		freeFunc ?? {freeFunc = `nil};
		synth.onFree({this.freeResources});
	}

	outputBus_{|newBus|
		outputBus = newBus;
		if(this.isRunning, {synth.set(\out, outputBus)});
	}

	isRunning { synth !? {^synth.isRunning} ?? {^false} }

	isPlaying { ^synth.isPlaying }

	lag_{|newLag = 0.01|
		lag = newLag;
		this.play;
		if(canPlay){
			synth !? {
				if(synth.isPlaying){
					synth.set(\lag, newLag);
				};
			};
		};
	}

	*pr_DefineSynthDefShell{ |toWrap|
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
	}

	pr_FreeGroupAndBus{
		if(group.isNil.not){
			if(group.isPlaying){
				group.free;
			};
			group = nil;
		};

		if(inputBus.isNil.not){
			if(inputBus.class==Bus){
				if(inputBus.index.isNil.not){
					inputBus.free;
				};
			};
			inputBus=nil;
		};

		if(outputBus.isNil.not){
			if(outputBus.class==Bus){
				if(outputBus.index.isNil.not){
					outputBus.free;
				};
			};
			outputBus = nil;
		};
		isFreed = true;
	}

	pr_FreeResources{
		super.free;
		spatialCellInstances.remove(this);
		if(freeFunc.value.isNil.not){
			freeFunc.value.value;
		};
		this.pr_FreeGroupAndBus;
	}

	onFree{|function|
		if(freeFunc.isNil){
			freeFunc = `function;
		}/*ELSE*/{
			freeFunc.value = freeFunc.value ++ function;
		};
	}

	free{
		if(synth.isPlaying){
			if(synth.isRunning){
				doneAction = 2;
				this.awaken;
			}/*ELSE*/{
				//if it is not running, then just free everything all at once
				synth.free;
			};
		}/*ELSE*/{
			this.pr_FreeResources;
		};
	}

	*freeAll{
		if(spatialCellInstances!=nil){
			if(spatialCellInstances.size > 0){
				//free all spatialCellInstances of this class
				spatialCellInstances.copy.do{|instance|
					instance.free;
				};
			};
		};
	}

	azimuth_{|newAzimuth = pi|
		azimuth = newAzimuth;
		if(canPlay){
			synth !? {
				if(synth.isPlaying){
					synth.set(\azimuth, azimuth.wrap(-pi, pi));
				};
			};
		};
	}

	elevation_{ |newElevation = 0|
		elevation = newElevation;
		if(canPlay){
			synth !? {
				if(synth.isPlaying){
					synth.set(\elevation, elevation.wrap(-pi, pi));
				};
			};
		};
	}

	distance_{ |newDistance|
		distance = newDistance;
		if(canPlay){
			synth !? {
				if(synth.isPlaying){
					synth.set(\distance, distance.clip(1.0, 100.0));
				};
			}
		}
	}

	awaken {
		if(this.isRunning.not, {  
			if(pausingRoutine.isPlaying){			
				pausingRoutine.stop;
			};
			pausingRoutine = Routine({
				synth.set(\doneAction, 0, \wakeUp, 1);
				synth.run(true);
				(server.latency * 2).wait;
				synth.set(\doneAction, doneAction);				});
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

	*instances{
		^spatialCellInstances;
	}
}
