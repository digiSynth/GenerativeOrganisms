GenOrgGene : GenOrgHybrid {
	var <incrementer, <options, cleanup, <>timescale = 1;

	*contribute { | versions |
		var path = Main.packages.asDict.at(\GenOrg)
		+/+"Classes/GenOrgGene";

		versions.add(
			[\morph, path+/+"morph"]
		);

		versions.add(
			[\amp_mod, path+/+"amp_mod"]
		);

	}

	*makeTemplates { | templater |
		templater.gene_function;
	}

	initGenOrgHybrid {
		cleanup = List.new;
		options = server.options.copy
		.verbosity_(-2)
		.sampleRate_(48e3)
		.numOutputBusChannels_(1)
		.recSampleFormat_("int24");
		incrementer = incrementer ?? {
			CodexIncrementer(
				"mutation.wav",
				"~/mutations".standardizePath
			);
		};
	}

	fileTemplate {
		^incrementer.fileTemplate;
	}

	fileTemplate_{ | newTemplate |
		incrementer.fileTemplate = newTemplate;
	}

	folder {
		^incrementer.folder;
	}

	folder_{ | newFolder |
		incrementer.folder = newFolder;
	}

	getSynthMsg { | buffer0, buffer1 |
		^((Synth.basicNew(modules.synthDef.name)).newMsg(
			args: this.getArguments(buffer0, buffer1);
		));
	}

	getArguments  { | buffer0, buffer1 |
		var array = [];
		modules.synthDef.specs.keysValuesDo({ | key, value |
			array = array.add(key);
			array = array.add(value.map(1.0.rand));
		});
		^(array++[
			\buffer0, buffer0,
			\buffer1, buffer1,
			\timescale, timescale
		]);
	}

	mutate { | buffer0, buffer1, action |
		var oscpath = PathName.tmp +/+ UniqueID.next ++ ".osc";
		var outpath = incrementer.increment;
		var reference = `nil;
		this.getScore(buffer0, buffer1, timescale).recordNRT(
			oscpath,
			outpath,
			nil,
			48e3,
			"wav",
			"int24",
			options,
			"",
			timescale,
			action: {
				cleanup.add({ File.delete(oscpath) });
				cleanup.do(_.value);
				cleanup.clear;
				fork {
					var condition = Condition.new;
					var buffer = Buffer.read(
						server,
						outpath,
						action: { | buffer |
							condition.unhang;
							action.value(buffer);
						}
					);
					condition.hang;
					reference.value = buffer;
				};
			}
		);
		^reference;
	}

	getScore { | buffer0, buffer1, timescale |
		var score = Score.new;
		var buffer0Copy = Buffer(server, server.sampleRate, 1);
		var buffer1Copy = Buffer(server, server.sampleRate, 1);

		score.add([
			0, [\d_recv, modules.synthDef.asBytes]
		]);

		score.add([
			0, buffer0Copy.allocMsg,
			buffer0Copy.readMsg(buffer0.path);
		]);

		score.add([
			0, buffer1Copy.allocMsg,
			buffer1Copy.readMsg(buffer1.path);
		]);

		score.add([
			0, this.getSynthMsg(buffer0Copy, buffer1Copy)
		]);

		cleanup.add({
			buffer0Copy.free;
			buffer1Copy.free;
		});

		^score.sort;
	}

	buildSynthDef {
		^SynthDef.new(\synth, {
			var timescale = \timescale.kr(1);
			var buffer0 = \buffer0.kr(0);
			var buffer1 = \buffer1.kr(1);
			var env = Env(
				[0, 1, 1, 0],
				[0.1, 1, 0.1].normalizeSum,
				\welch
			).ar(
				timeScale: timescale,
				doneAction: Done.freeSelf;
			);
			var sig = modules.gene_function(buffer0, buffer1, timescale);
			Out.ar(\out.kr(0), sig * env);
		});
	}
}
