SpaceCell : LiveCodingEnvironment{
	classvar <classSymbol, <instances;
	classvar <spatializersInit = false;
	// classvar encoder, decoder;

	var freeFunc;
	var <group, <inputBus, <outputBus, <synth, <event = nil, <function = nil;
	var canPlay = false, <isFreed = false;
	var <isRunning = true;
	var pausingRoutine = nil;
	var <>distanceMaxFreq = 16000;
	var <lag, <azimuth, <elevation, <distance;

	var <mover;

	*new{|symbol|
		var return;

		instances = instances ? List.new;
		//call an instance of this class
		return = super.new(symbol).pr_SetupSpaceCell;

		//add that instance to the class's list of instances
		this.pr_InitializeSpaceCell(return);
		instances.add(return);

		//return it
		^return;
	}

	*initNew{ |symbol|
		var toremove;
		this.pr_InitializeSpaceCell;
		toremove = super.new(symbol);
		super.pr_RemoveInstance(toremove);
	}

	reInitialize{
		if(isFreed==false){
			if(isRunning==false){
				this.pr_SetupSpaceCell;
			}/*ELSE*/{
				("Warning: Spatializer is already running"
					++" and so cannot be reinitialized.").postln;
			};
		};
	}

	*pr_InitializeSpaceCell{|toAdd|
		//adds a copy to manage all instances of active particles
		instances = instances ? List.new;

		//set up the class symbol:
		//this will be used to format synth names as well as to
		//manage loading synthdefs onto the server by the super
		//class so that every calling of the class does
		//not also accompany the reloading of redundant synthdefs
		instances.add(toAdd);
	}

	pr_SetupSpaceCell{
		var returnFunction = {

			//load and process the event and function
			// this.prSetUpEventOrFunction(eventOrFunction);

			//allocate server resources for the class
			this.pr_MakeBusses;
			this.pr_MakeNodes;

			CmdPeriod.doOnce({
				if(isRunning){
					isRunning = false;
				};
			});

		};

		if(spatializersInit==false){
			forkIfNeeded{
				SpaceCell.loadSpaceCellSynthDefs;
				server.sync;
				returnFunction.value;
			};
		}/*ELSE*/{
			returnFunction.value;
		};
	}

	pr_MakeNodes{
		isRunning = false;
		canPlay = false;

		// server.bind({
		this.pr_MakeGroups;
		this.pr_MakeSynth;
		isRunning = true;
		// });

	}

	pr_MakeBusses{

		if(inputBus.isNil){
			inputBus = Bus.audio(server, 1);
		};

		if(inputBus.index.isNil){
			inputBus = Bus.audio(server, 1);
		};

		outputBus = outputBus ? 0;
	}

	outputBus_{|newBus|
		outputBus = newBus;

		if(synth.isRunning){
			synth.set(\out, outputBus);
		};
	}

	pr_MakeGroups{|condition|
		//load a dictionary of groups

		if(isRunning==false){
			group = Group.new;
			group.register;
		};

	}

	pr_MakeSynth{
		//load the synth.
		lag = lag ? (server.latency * 0.1);

		synth = Synth.newPaused(this.formatSynthName(\Synth), [

			\in, inputBus,
			\out, outputBus,
			\angle, pi/2,

			\lag, lag,
			\timer, server.latency,
			// \distance, 1e-3,
			\doneAction, 0,

		], group).register;

		canPlay = true;
	}

	lag_{|newLag = 0.01|
		lag = newLag;

		this.playSpaceCell;

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
			if(inputBus.index.isNil.not){
				inputBus.free;
			};
			inputBus=nil;
		};

		if(outputBus.class==Bus){
			if(outputBus.isNil.not){
				outputBus.free;
				outputBus = nil;
			};
		};
	}

	pr_FreeResources{
		super.free;
		instances.remove(this);
		this.pr_FreeGroupAndBus;
		isRunning = false;
		synth = nil;
		isFreed = true;
	}

	onFree{|function|
		if(freeFunc.isNil){
			freeFunc = function;
		}/*ELSE*/{
			freeFunc = freeFunc ++ function;
		};
	}

	free{

		synth.onFree({

			freeFunc !? {
				freeFunc.value
			};

			this.pr_FreeResources;
		});


		if(synth.isPlaying){

			if(synth.isRunning){
				//set a random release time
				var release = lag * 1.05;

				//release the synth at that time
				synth.set(\release, release, \doneAction, 2);

			}/*ELSE*/{

				//if it is not running, then just free everything all at once
				synth.free;
			};

		}/*ELSE*/{

			if(group.isPlaying){
				this.pr_FreeResources;
			};

			freeFunc.value;

		};
	}

	*freeAll{
		if(instances!=nil){

			if(instances.size > 0){
				//free all instances of this class
				instances.copy.do{|instance|
					instance.free;
				};

			};

		}

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

	/*	position{|az, el, d|

	this.azimuth = azimuth;
	this.elevation = el;
	this.distance = d;

	}*/

	pr_WakeUpSynthAndPlay{
		//this was a pain in the ass to figure out.
		//this method makes sure that the synth both will play when called but also
		//limits its overall impact on the server resources when not in use


		//if the synth exists
		synth !? {

			//if it is running on the server
			if(synth.isRunning){

				//then play the event through it
				// event.play;
			}/*ELSE*/{

				//see if it is in the process of being paused
				pausingRoutine !? {
					//if it is being paused, stop that routine
					if(pausingRoutine.isPlaying){
						pausingRoutine.stop;
						pausingRoutine = nil;
					};
					//and then...
				};

				//wake up the synth:
				pausingRoutine = fork{

					//this routine wakes up the synth by setting its doneAction to "none"
					//and routing pink noise through the DetectSilence UGen.
					synth.set(\doneAction, 0, \wakeUp, 1);
					synth.run(true);


					//after one period of server latency, the doneAction is set to "pauseSelf"
					//and, after another period of server latency, the synth pauses itself freeing up resources
					(server.latency * 2).wait;
					synth.set(\doneAction, 1);

					//once the routine is finished, the variable in which it was stored is set to nil again
					pausingRoutine = nil;
				};
			};
		};
	}

	playSpaceCell{

		if(canPlay){

			this.pr_WakeUpSynthAndPlay;

		};

	}

	*loadSpaceCellSynthDefs{

		// Routine({
		var synthArray = this.subclasses.do{|item|

			synthDefDictionary !? {
				if(synthDefDictionary[item.asSymbol].isNil.not){
					synthDefDictionary.removeAt(item.asSymbol);
				};
			};

			item.initNew;
		};


		ServerQuit.add({
			spatializersInit = false;
		});

		spatializersInit = true;
	}

	*postSynthsAndControls{
		var targetList = synthDefDictionary[classSymbol];

		if(targetList.isNil.not){

			targetList.do{|synthDef|
				super.postControlNames(synthDef.name, synthDef.name);
			};

		};
	}

	*postControlNames{|synthDefName|
		var controlNameArray = this.getControlNames(synthDefName, classSymbol);
		format("SynthDef name: %", synthDefName).postln;
		"Control names: [\n".post;
		controlNameArray.do{|item, index|
			"\t\t".post; item.post;
			if(index < (controlNameArray.size - 1)){
				",".post;
			};
			"\n".post;
		};
		"];\n".postln;
	}

	mover_{|inputSpaceCellMover|

		if(inputSpaceCellMover.isNil.not){
			if(inputSpaceCellMover.class!=SpaceCellMover){
				Error("Can only supply a SpaceCellMover to this field").throw;
			};
		};

		mover = inputSpaceCellMover;

	}

	map{|targetSpaceCellMover|

		if(targetSpaceCellMover.class!=SpaceCellMover){
			Error("Can only map a SpaceCellMover to a SpaceCell").throw;
		};

		targetSpaceCellMover.mapTo(this);

	}


	unmap{

		if(mover.isNil.not){
			mover.free;
		};

		mover = nil;
	}

}