//Ideally, I can remove the subclasses of this class and just make it so that this class is configureable easily. That way, I can load two of them into organism class for mating with and consuming others. I can configure them there and create interfaces for reconfiguring them through the language. That way, the way things are mutating can itself easily mutate.

GenOrg_Mutator{
	var server, incrementer;
	var folderPath, <synthDef;
	var isInitialized = false;

	*new{|synthDef|
		^super.new
		.pr_InitGenOrg_Mutator
		.synthDef_(synthDef);
	}

	synthDef_{|newSynthDef|

		if(newSynthDef.isNil){
			newSynthDef = this.pr_DefaultSynthDef;
		};

		if(newSynthDef.isKindOf(SynthDef).not){
			Error("Can only set to object of kind of SynthDef").throw;
		};

		synthDef = newSynthDef;
		this.pr_ProcessSynthDefMetaData;
	}

	render{ |buffer0, buffer1, duration = 1, action|
		var reference = Ref.new(nil);

		if(server.hasBooted.not){
			isInitialized = false;
			Error("Server has not booted").throw;
		};

		if(isInitialized){
			var condition;
			this.pr_GenOrg_ProcessAudio(reference, duration, buffer0, buffer1, action);
		}/*ELSE*/{
			this.pr_InitGenOrg_Mutator;
			^this.render(buffer0, buffer1, duration, action);
		};

		^reference;
	}

	pr_InitGenOrg_Mutator{
		server = server ? Server.default;

		incrementer = incrementer ? FileIncrementer.new(
			"organism-render.wav",
			GenOrg_Mutator_AudioPath.path
		);

		if(File.exists(GenOrg_Mutator_AudioPath.path).not){
			File.mkdir(GenOrg_Mutator_AudioPath.path);
		};
		isInitialized = true;
	}

	pr_GenOrg_ProcessAudio{|reference, duration, buffer0, buffer1, action|
		var timescale = duration;
		var score = Score.new;

		var buffer0Copy = Buffer.new(server, server.sampleRate * duration, 1);
		var buffer1Copy = Buffer.new(server, server.sampleRate * duration, 1);

		var oscpath = PathName.tmp +/+ UniqueID.next ++".osc";
		var outpath = incrementer.nextFileName;
		var scoreAction = {

				format("\n% rendered\n",
					PathName(outpath)
					.fileNameWithoutExtension
				).postln;

		};

		var buf0FileStartFrame = 0, buf1FileStartFrame = 0;
		var synthMsg = this.pr_GetSynthMsg
		(buffer0Copy, buffer1Copy, duration);
		var synthDefName = synthDef.name;
		var timeoffset = 1e-5;

		action = scoreAction++action;

		if((buffer0.numFrames / server.sampleRate) > duration){
			buf0FileStartFrame =
			(buffer0.numFrames - (server.sampleRate * duration)).rand.floor;
		};

		if((buffer1.numFrames / server.sampleRate) > duration){
			buf1FileStartFrame =
			(buffer1.numFrames - (server.sampleRate * duration)).rand.floor;
		};

		score.add([
			0, buffer0Copy.allocMsg,
			buffer0Copy.readMsg(buffer0.path, buf0FileStartFrame);
		]);

		score.add([
			0, buffer1Copy.allocMsg,
			buffer1Copy.readMsg(buffer1.path, buf1FileStartFrame);
		]);

		score.add([
			0, [\d_recv, synthDef.asBytes]
		]);

		score.add([
			timeoffset, synthMsg
		]);

		score.add([
			timescale+timeoffset, [1];
		]);

		score.sort;

		this.pr_RenderMutatorScore(
			score,
			reference,
			buffer0Copy,
			buffer1Copy,
			timescale+timeoffset,
			outpath,
			oscpath,
			action
		);

	}

	pr_RenderMutatorScore{|score, reference, buffer0, buffer1,
		duration, outpath, oscpath, action|

		score.recordNRT(
			oscpath,
			outpath,
			nil,
			48e3,
			"wav",
			"int24",
			ServerOptions.new
			.numInputBusChannels_(0)
			.numOutputBusChannels_(1)
			.sampleRate_(48e3)
			.memSize_(2.pow(19))
			.numWireBufs_(2.pow(13))
			.verbosity_(-2),
			action: {

				forkIfNeeded{
					var localCondition = Condition.new;
					var toReturn;

					buffer0.freeMsg;
					buffer1.freeMsg;

					File.delete(oscpath);

					toReturn = Buffer.read(server, outpath, action: {
						localCondition.unhang;
						reference.value = toReturn;
						action.value;
					});

					localCondition.hang;
				};
			};
		);
	}

	pr_GetSynthMsg{|buffer0, buffer, timescale = 1|
		var name, argumentArray;
		if(synthDef.isNil){
			synthDef = this.pr_DefaultSynthDef;
		};

		name = synthDef.name;

		argumentArray = synthDef.metadata.copy;
		argumentArray.keys.do{|key|
			var item = argumentArray[key];
			argumentArray[key] = item.map(1.0.rand);
		};

		argumentArray = argumentArray.asPairs;

		^((Synth.basicNew(name)
		).newMsg(args: argumentArray));

	}

	pr_DefaultSynthDef{
		^SynthDef(
			format("GenOrg_MutatorSynth%", UniqueID.next).asSymbol, {
				var timescale = \timescale.kr(1);
				var buf0 = \buf0.kr(0);
				var buf1 = \buf1.kr(1);
				var sig0 = PlayBuf.ar(1, buf0,
					BufRateScale.kr(buf0) * \rate0.kr(1));
				var sig1 = PlayBuf.ar(1, buf1,
					BufRateScale.kr(buf1) * \rate1.kr(1));
				var env = EnvGen.ar(Env([0, 1, 1, 0],
					[0.05, 1, 0.05].normalizeSum),
				timeScale: timescale,
				doneAction: Done.freeSelf
				);
				var chain0 = FFT(LocalBuf(2.pow(10), 1), sig0);
				var chain1 = FFT(LocalBuf(2.pow(10), 1), sig1);
				var morph = PV_Morph(chain0, chain1,
					LFNoise2.kr(timescale * \morphCycles.kr(8)).unipolar(1)
				);
				var out = IFFT(morph) * env * \ampDB.kr(0).dbamp;
				out = Normalizer.ar(out, 1.0);
				Out.ar(\out.kr(0), out);
			},
			metadata:
			(
				rate0: ControlSpec(0.05, 3.0, 'exp'),
				rate1: ControlSpec(0.05, 3.0, 'exp'),
				timescale: ControlSpec(1, 1),
				morphCycles: ControlSpec(2.0, 32.0, 'exp'),
				out: ControlSpec(0, 0),
				ampDB: ControlSpec(0, 0)
			)
		)
	}

	pr_ProcessSynthDefMetaData{
		var metadata = synthDef.metadata;
		metadata[\buf0] = ControlSpec(0, 0, 'lin', default: 0);
		metadata[\buf1] = ControlSpec(1, 1, 'lin', default: 1);
		synthDef.metadata = metadata;
	}
}

GenOrg_Mutator_AudioPath : FileConfigurer{
	classvar id;
	classvar internalPath;

	*defaultPath{
		^("~/Desktop/audio/organism-mutations".standardizePath);
	}

	*path{
		if(internalPath.isNil){
			this.pr_SetID;
			internalPath = super.path(id);
		};

		^internalPath;
	}

	*path_{|newpath|

		internalPath = nil;
		this.pr_SetID;
		^super.path_(newpath, id);

	}

	*pr_SetID{
		if(id.isNil){
			id = super.pr_SetID;
		};
	}

	*id{
		this.pr_SetID;
		^id;
	}
}