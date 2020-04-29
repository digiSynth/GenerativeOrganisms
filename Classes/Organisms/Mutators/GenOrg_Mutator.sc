GenOrg_Mutator{
	var server, incrementer;
	var folderPath, <synthDef;
	var isInitialized = false;

	*new{|synthDef|
		^super.new
		.synthDef_(synthDef)
		.pr_InitGenOrg_Mutator
	}

	synthDef_{|newSynthDef|

		if(newSynthDef.isKindOf(SynthDef).not and: {newSynthDef.isNil.not}){
			Error("Can only set object of kind SynthDef").throw;
		};

		synthDef = newSynthDef;

	}

	render{ |buffer0, buffer1, duration = 1, action|
		var return = Ref.new(nil);

		if(server.hasBooted.not){
			isInitialized = false;
			Error("Server has not booted").throw;
		};

		if(isInitialized){
			this.pr_GenOrg_ProcessAudio(return, duration, buffer0, buffer1, action);
		}/*ELSE*/{
			this.pr_GenOrg_MutatorInit;
			this.render(buffer0, buffer1, duration, action);
		};

		while({return.value.isNil}, {});
		return = return.value;

		^return;
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

	/*	pr_GenOrg_ProcessAudio{|reference, duration = 1, action|
	this.subclassResponsibility(thisMethod);
	}*/

	/*
	pr_GenOrg_ProcessAudio{|reference, duration = 1, buffer0, buffer1, action|
	var timescale = duration ? 1.0;
	var score = this.pr_InitScore;
	score = this.pr_AddSynthToScore(score, timescale, buffer0, buffer1);
	this.pr_RenderNRT(score, reference, timescale, buffer0, buffer1, action);
	^reference;
	}*/
	pr_InitScore{|duration, buffer0, buffer1|
		var timescale = duration;
		var score = Score.new;

		var buffer0Copy = Buffer.new(server, server.sampleRate * duration, 1);
		var buffer1Copy = Buffer.new(server, server.sampleRate * duration, 1);

		var oscpath = PathName.tmp +/+ UniqueID.next ++".osc";
		var outpath = incrementer.nextFileName;

		var buf0FileStartFrame = 0, buf1FileStartFrame = 0;
		var synthDefName = synthDef.name;

		if((buffer0.numFrames / server.sampleRate) > 1.0){
			buf0FileStartFrame = (buffer0.numFrames - server.sampleRate).rand.floor;
		};

		if((buffer1.numFrames / server.sampleRate) > 1.0){
			buf1FileStartFrame = (buffer1.numFrames - server.sampleRate).rand.floor;
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
			timescale, [1];
		]);

		^(score:score, buffer0: buffer0Copy, buffer1: buffer1Copy,
			oscpath: oscpath, outpath: outpath);
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

		score.add([
			0, this.pr_GetSynthMsg(buffer0, buffer1, duration);
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
					});

					localCondition.hang;
					reference.value = toReturn;

					action.value;

				}
			}
		);
	}
}

GenOrg_Mutator_AudioPath : FileConfigurer{
	classvar internalPath;

	*defaultPath{
		^("organism-mutations".ianAudioPath)
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


