//Ideally, I can remove the subclasses of this class and just make it so that this class is configureable easily. That way, I can load two of them into organism class for mating with and consuming others. I can configure them there and create interfaces for reconfiguring them through the language. That way, the way things are mutating can itself easily mutate.

GenOrg_Mutator{
	var server, incrementer;
	var folderPath, <synthDef;
	var isInitialized = false;

	*new{
		^super.new
		.pr_InitGenOrg_Mutator
	}

	synthDef_{|newSynthDef|

		if(newSynthDef.isNil){
			newSynthDef = this.pr_DefaultSynthDef;
		};

		if(newSynthDef.isKindOf(SynthDef).not){
			Error("Can only set to object of kind of SynthDef").throw;
		};

		newSynthDef.name = this.pr_DefaultSynthDefName;

		synthDef = newSynthDef;

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
			this.pr_GenOrg_MutatorInit;
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

	pr_InitScore{|duration, buffer0, buffer1|
		var timescale = duration;
		var score = Score.new;

		var buffer0Copy = Buffer.new(server, server.sampleRate * duration, 1);
		var buffer1Copy = Buffer.new(server, server.sampleRate * duration, 1);

		var oscpath = PathName.tmp +/+ UniqueID.next ++".osc";
		var outpath = incrementer.nextFileName;
		var action = {

			{
				format("\n% rendered\n",
					PathName(outpath)
					.fileNameWithoutExtension
				).postln;
			};

		};

		var buf0FileStartFrame = 0, buf1FileStartFrame = 0;
		var synthDefName = synthDef.name;

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
			timescale, [1];
		]);

		^(score:score, buffer0: buffer0Copy, buffer1: buffer1Copy,
			oscpath: oscpath, outpath: outpath, action: action);
	}

	//This is the method that subclasses will define
	//(along with setting a default SynthDef and default SynthDef name)
	pr_GetSynthMsg{|buffer0, buffer, timescale = 1|
		this.subclassResponsibility(thisMethod);
	}

	pr_DefaultSynthDefName{
		this.subclassResponsibility(thisMethod);
	}

	pr_DefaultSynthDef{
		this.subclassResponsibility(thisMethod);
	}

	pr_GenOrg_ProcessAudio{|reference, duration = 1, buffer0, buffer1, action|
		var scoreEvent = this.pr_InitScore(duration, buffer0, buffer1);
		var score = scoreEvent.score;
		var buffer0Copy = scoreEvent.buffer0;
		var buffer1Copy = scoreEvent.buffer1;
		var outpath = scoreEvent.outpath;
		var oscpath = scoreEvent.oscpath;
		var scoreAction = scoreEvent.action;

		var synthMsgEvent = this.pr_GetSynthMsg(buffer0Copy, buffer1Copy, duration);
		var synthMsg = synthMsgEvent.synthMsg;
		var synthDefToAdd = synthMsgEvent.synthDef;

		action = scoreAction++action;

		score.add([
			0, [\d_recv, synthDefToAdd.asBytes]
		]);

		score.add([
			0, synthMsg
		]);

		score.sort;

		this.pr_RenderMutatorScore(
			score,
			reference,
			buffer0Copy,
			buffer1Copy,
			duration,
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
}

GenOrg_Mutator_AudioPath : FileConfigurer{
	classvar internalPath;

	*defaultPath{
		^("~/Desktop/audio/organism-mutations".standardizePath);
	}

	*path{
		if(internalPath.isNil){
			internalPath = super.path;

			if(internalPath.isNil){
				while({internalPath.isNil}, {internalPath = super.path});
			};

		};

		^internalPath;
	}

	*path_{|newpath|

		internalPath = nil;
		^super.path_(newpath);

	}
}