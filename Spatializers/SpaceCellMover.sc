SpaceCellMover : LiveCodingEnvironment{
	classvar <instances, <isInitialized = false;

	var <azimuthMin, <azimuthMax;
	var <elevationMin, <elevationMax;
	var <distanceMin, <distanceMax;

	var <azimuthSynth, <elevationSynth, <distanceSynth;
	var <azimuthBus, <elevationBus, <distanceBus;

	var <azimuthRate = 0.5, <elevationRate = 0.5, <distanceRate = 0.5;

	var group, mappedSpaceCell;

	var <lag = 0.1;

	*new{|targetSpaceCell, azimuthRate = 0.5, elevationRate = 0.5, distanceRate = 0.5|
		var return;


		return = super.new
		(this.prFormatClassSymbol(this))
		.pr_InitSpaceCellMover(targetSpaceCell, azimuthRate, elevationRate, distanceRate);

		instances = instances ? List.new;
		instances.add(return);


		^return;

	}


	pr_InitSpaceCellMover{|targetSpaceCell, ar, er, dr|

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
				returnFunction.value(targetSpaceCell);
				server.sync;
				isInitialized = true;
			};

		}/*ELSE*/{
			returnFunction.value(targetSpaceCell);
		}

	}

	mapTo{|inputSpaceCell|

		if(inputSpaceCell.isNil.not){
			if(inputSpaceCell.isSpaceCell.not){
				Error("Can only map a SpaceCellMover to a SpaceCell.").throw;
			};

			if(mappedSpaceCell.isNil.not){
				this.unmap;
			};

			if(this.isPlaying){

				this.group = inputSpaceCell.group;

				inputSpaceCell.playSpaceCell;
				inputSpaceCell.synth.map(\azimuth, azimuthBus);
				inputSpaceCell.synth.map(\elevation, elevationBus);
				inputSpaceCell.synth.map(\distance, distanceBus);

				inputSpaceCell.mover_(this);
				mappedSpaceCell = inputSpaceCell;

			}

		}

	}

	unmap{

		if(mappedSpaceCell.isNil.not){
			mappedSpaceCell.set(\azimuth, pi.rand, \elevation, (pi/2).rand,
				\distance, exprand(1.0, 3.0));


			mappedSpaceCell.mover = nil;
			mappedSpaceCell = nil;

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
				}
			}
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

		if(mappedSpaceCell.isNil.not){
			if(mappedSpaceCell.mover==this){
				mappedSpaceCell.mover = nil;
			};
		};

		super.free;
		instances.remove(this);
	}


	*freeAll{
		if(instances.isNil.not){

			instances.copy.do{|item|

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


}