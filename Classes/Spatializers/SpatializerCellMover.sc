SpatialCellMover : LiveCodingEnvironment{
	classvar moverInstances, <isInitialized = false;

	var <azimuthMin, <azimuthMax;
	var <elevationMin, <elevationMax;
	var <distanceMin, <distanceMax;

	var <azimuthSynth, <elevationSynth, <distanceSynth;
	var <azimuthBus, <elevationBus, <distanceBus;

	var <azimuthRate = 0.5, <elevationRate = 0.5, <distanceRate = 0.5;

	var group, mappedSpatialCell;

	var <lag = 0.1;

	*new{|targetSpatialCell, azimuthRate = 0.5, elevationRate = 0.5, distanceRate = 0.5|
		var return;

		return = super.new
		(this.prFormatClassSymbol(this))
		.pr_InitSpatialCellMover(targetSpatialCell, azimuthRate, elevationRate, distanceRate);

		moverInstances = moverInstances ? List.new;
		moverInstances.add(return);


		^return;

	}


	pr_InitSpatialCellMover{|targetSpatialCell, ar, er, dr|

		var returnFunction = {|target|

			this.pr_MakeBusses;
			this.pr_MakeSynths(ar, er, dr);

			target !? {

				forkIfNeeded{

					server.sync;

					this.mapTo(target);

				};
			};

		};

		if(isInitialized==false){

			forkIfNeeded{

				server.sync;

				returnFunction.value(targetSpatialCell);

				server.sync;

				isInitialized = true;

			};

		}/*ELSE*/{

			returnFunction.value(targetSpatialCell);

		}

	}

	mapTo{|inputSpatialCell|

		if(inputSpatialCell.isNil.not){

			if(inputSpatialCell.isSpatialCell.not){

				Error("Can only map a SpatialCellMover to a SpatialCell.").throw;

			};

			if(mappedSpatialCell.isNil.not){

				this.unmap;

			};

			if(this.isPlaying){

				this.group = inputSpatialCell.group;

				inputSpatialCell.synth.map(\azimuth, azimuthBus);
				inputSpatialCell.synth.map(\elevation, elevationBus);
				inputSpatialCell.synth.map(\distance, distanceBus);

				inputSpatialCell.mover_(this);
				mappedSpatialCell = inputSpatialCell;

			};

		}

	}

	unmap{

		if(mappedSpatialCell.isNil.not){
			mappedSpatialCell.set(\azimuth, pi.rand, \elevation, (pi/2).rand,
				\distance, exprand(1.0, 3.0));


			mappedSpatialCell.mover = nil;
			mappedSpatialCell = nil;

			group = server.defaultGroup;
		};

	}

	isPlaying{


		[azimuthSynth, elevationSynth, distanceSynth,
			azimuthBus, elevationBus, distanceBus].do{|item|

			if(item.isNil){

				^false;

			};

			if(item.class==Synth){
				if(item.isPlaying.not){

					^false;

				}
			};

			if(item.class==Bus){
				if(item.index.isNil){

					^false;

				};

			};

		};

		^true;

	}

	pr_MakeBusses{
		azimuthBus = this.pr_MakeSingleBus(azimuthBus);
		elevationBus = this.pr_MakeSingleBus(elevationBus);
		distanceBus = this.pr_MakeSingleBus(distanceBus);
	}

	pr_MakeSingleBus{
		|input|


		if(input.class==Bus){

			if(input.index.isNil.not){

				input.free;

			};

		};

		^Bus.control(server, 1);
	}

	pr_MakeSynths{|ar, er, dr|
		var symbol = this.class.prFormatClassSymbol(this.class);
		var target = group ? server.defaultGroup;

		azimuthSynth = Synth(
			this.class.formatSynthName(\azimuthSynth, symbol),
			[\out, azimuthBus, \rate, ar], target: target
		).register.onFree({

			azimuthBus.free;
			this.pr_checkIfAllFree;

		});

		azimuthRate = ar;

		elevationSynth = Synth(
			this.class.formatSynthName(\elevationSynth, symbol),
			[\out, elevationBus, \rate, er], target: target
		).register.onFree({

			elevationBus.free;
			this.pr_checkIfAllFree;

		});

		elevationRate = er;

		distanceSynth = Synth(
			this.class.formatSynthName(\distanceSynth, symbol),
			[\out, distanceBus, \rate, dr], target: target
		).register.onFree({

			distanceBus.free;
			this.pr_checkIfAllFree;

		});

		distanceRate = dr;

	}

	azimuthRate_{|newRate = 0.5|
		azimuthRate = newRate;

		if(azimuthSynth.isNil.not){
			if(azimuthSynth.isPlaying){
				azimuthSynth.set(\rate, azimuthRate);
			}
		}
	}


	elevationRate_{|newRate = 0.5|
		elevationRate = newRate;

		if(elevationSynth.isNil.not){
			if(elevationSynth.isPlaying){
				elevationSynth.set(\rate, elevationRate);
			}
		}
	}

	distanceRate_{|newRate = 0.5|
		distanceRate = newRate;

		if(distanceSynth.isNil.not){
			if(distanceSynth.isPlaying){
				distanceSynth.set(\rate, distanceRate);
			}
		}
	}

	pr_checkIfAllFree{
		var howManyRunning = 0;

		[azimuthSynth, elevationSynth, distanceSynth].do{
			|synth|

			if(synth.isNil.not){
				if(synth.isPlaying){
					howManyRunning = howManyRunning + 1;
				}
			}
		};

		if(howManyRunning==0){
			this.free;
		};
	}

	group_{|newGroup|
		var previousGroup;

		if(group.isNil.not){
			previousGroup = group;
		};

		group = newGroup;

		[azimuthSynth, elevationSynth, distanceSynth].do{
			|synth|
			if(synth.isNil.not){
				if(synth.isPlaying){
					synth.moveToHead(group);
				}
			}
		}
	}

	pr_FreeSynths{

		[azimuthSynth, elevationSynth, distanceSynth].do{|synth|
			if(synth.isNil.not and: {synth.isPlaying}){

				synth.free;

			};

		};

		#azimuthSynth, elevationSynth, distanceSynth = [nil, nil, nil];

	}

	pr_FreeBusses{

		[azimuthBus, elevationBus, distanceBus].do{|bus|

			if(bus.isNil.not and: {bus.index.isNil.not}){

				bus.free;

			};

		};

		#azimuthBus, elevationBus, distanceBus = [nil, nil, nil];

	}

	free{

		this.pr_FreeSynths;
		this.pr_FreeBusses;

		if(mappedSpatialCell.isNil.not){
			if(mappedSpatialCell.mover==this){
				mappedSpatialCell.mover = nil;
			};
		};

		super.free;
		moverInstances.remove(this);
	}


	*freeAll{
		if(moverInstances.isNil.not){

			moverInstances.copy.do{|item|

				item.free;

			};
		};

	}

	lag_{|newVal|
		lag = newVal;

		[azimuthBus, elevationBus, distanceBus].do{|synth|

			if(synth.isNil.not and: {synth.isPlaying}){
				synth.set(\lag, lag);
			};

		}
	}


	*defineSynthDefs{
		var symbol = this.prFormatClassSymbol(this);
		var synthDef;

		synthDef = SynthDef(\azimuthSynth, {

			var lag = \lag.kr(0.05);

			Out.kr(\out.kr(0), LFNoise2.kr(\rate.kr(0.5, lag)) * pi
				* \scalar.kr(1, lag));

		});

		this.registerSynthDef(synthDef, false, symbol);

		synthDef = SynthDef(\elevationSynth, {

			var lag = \lag.kr(0.05);

			Out.kr(\out.kr(0), (LFNoise2.kr(\rate.kr(0.5, lag)) * 0.5 * pi
				* \scalar.kr(1, lag))
			);

		});

		this.registerSynthDef(synthDef, false, symbol);

		synthDef = SynthDef(\distanceSynth, {

			var lag = \lag.kr(0.05);

			Out.kr(\out.kr(0), (LFNoise2.kr(\rate.kr(0.5, lag)
				.exprange(1.0, 4.0))
			* \scalar.kr(1, lag)).clip(1.0, 4.0));

		});

		this.registerSynthDef(synthDef, false, symbol);
	}

	*instances{

		^moverInstances;

	}

}