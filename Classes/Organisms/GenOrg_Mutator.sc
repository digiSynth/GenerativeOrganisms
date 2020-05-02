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
		// this.pr_ProcessSynthDefMetaData;
	}

	mutate{ |buffer0, buffer1, duration = 1, action|
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

	pr_GetSynthMsg{|buffer0, buffer1, timescale = 1|
		var name, argumentArray;
		if(synthDef.isNil){
			synthDef = this.pr_DefaultSynthDef;
		};

		name = synthDef.name;

		argumentArray = synthDef.metadata;

		argumentArray.keys.do{|key|
			var item = argumentArray[key];
			argumentArray[key] = item.map(1.0.rand);
		};

		argumentArray = argumentArray.asPairs;

		if(argumentArray.containsIdentical(\buf0).not){
			argumentArray = argumentArray++[\buf0, buffer0.bufnum];
		};

		if(argumentArray.containsIdentical(\buf1).not){
			argumentArray = argumentArray++[\buf1, buffer1.bufnum];
		};

		^((Synth.basicNew(name)
		).newMsg(args: argumentArray));

	}

	pr_GenOrg_ProcessAudio{|reference, duration, buffer0, buffer1, action|

		var synth;
		var timescale = duration;
		var score = Score.new;

		var buffer0Copy = Buffer.new(server, server.sampleRate, 1);
		var buffer1Copy = Buffer.new(server, server.sampleRate, 1);

		var oscpath = PathName.tmp +/+ UniqueID.next ++".osc";
		var outpath = incrementer.nextFileName;

		var synthMsg = this.pr_GetSynthMsg(buffer0Copy, buffer1Copy);

		score.add([
			0, buffer0Copy.allocMsg,
			buffer0Copy.readMsg(buffer0.path);
		]);

		score.add([
			0, buffer1Copy.allocMsg,
			buffer1Copy.readMsg(buffer1.path);
		]);

		score.add([
			0, [\d_recv, synthDef.asBytes]
		]);

		score.add([
			0, synthMsg
		]);

		score.add([timescale, 0]);

		score.sort;

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
			.memSize_(2.pow(10))
			.numWireBufs_(2.pow(13))
			.verbosity_(-2),
			action: {
				fork{
					var localCondition = Condition.new;
					var toReturn;

					buffer0Copy.freeMsg;
					buffer1Copy.freeMsg;

					File.delete(oscpath);

					toReturn = Buffer.read(server, outpath, action: {
						localCondition.unhang;
					});

					localCondition.hang;
					reference.value = toReturn;

					format("\n% rendered\n",
						PathName(outpath)
						.fileNameWithoutExtension
					).postln;

					action.value;

				};

			}

		);

		^reference;
	}

	pr_DefaultSynthDef{

		var makeEnv = {|dur, da = 0|
			SynthDef.wrap({
				EnvGen.ar(Env([0, 1, 1, 0],
					[0.1, 1, 0.1].normalizeSum * dur),
				doneAction: da);
			});
		};

		^SynthDef(
			format("GenOrg_MutatorSynth%", UniqueID.next).asSymbol, {
				var timescale = \timescale.kr(1);
				var buf0 = \buf0.kr(0);
				var buf1 = \buf1.kr(1);
				var sig0 = PlayBuf.ar(1, buf0,
					BufRateScale.kr(buf0) * \rate0.kr(1))
				* makeEnv.value(BufDur.kr(buf0) * 0.99, 0);

				var sig1 = PlayBuf.ar(1, buf1,
					BufRateScale.kr(buf1) * \rate1.kr(1))
				* makeEnv.value(BufDur.kr(buf1) * 0.99, 0);

				var env = makeEnv.value(timescale, 2);
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
				ampDB: ControlSpec(-3, -3)
			)
		)
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