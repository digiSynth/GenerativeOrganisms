GenOrgMutator : GenOrgHybrid { 
	var incrementer, <options, cleanupList; 

	*makeTemplates { | templater | 
		templater.mutator_function;
	}

	initHybrid { 
		cleanupList = List.new;
		options = server.options.copy
		.verbosity_(-2)
		.sampleRate_(48e3)
		.recSampleFormat_("int24")
	}

	getSynthMsg { | buffer0, buffer1, timescale |
		this.processSynthArgs(buffer0, buffer1, timescale);
		^((Synth.basicNew(modules.synthDef.name)).newMsg(
			args: this.getArguments(buffer0, buffer1, timescale);
		));
	}

	getArguments  { | buffer0, buffer1, timescale | 
		var array = [];
		this.synthDef.specs.keysValuesDo({ | key, value |
			array = array.add(key);
			array = array.add(value.map(1.0.rand));
		});
		array = array++[
			\buffer0, buffer0, 
			\buffer1, buffer1, 
			\timescale, timescale
		];
		^array;
	}

	mutate { | buffer0, buffer1, timescale, action | 
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
				fork {
					var condition = Condition.new;
					var buffer = Buffer.read(
						server, 
						outpath, 
						action: { condition.unhang; }
					);
					condition.hang;
					reference.value = buffer;
					cleanupList.do(_.value); 
					cleanupList.clear;
				};
			}
		);
		^reference;
	}

	getScore { | buffer0, buffer1, timescale | 
		var score = Score.new; 
		var b0c = Buffer(server, server.sampleRate, 1); 
		var b1c = Buffer(server, server.sampleRate, 1);
		var synthMsg = this.getSynthMsg(buffer0, buffer1, timescale); 

		score.add([ 
			0, [\d_recv, modules.mutator.asBytes]
		]);

		score.add([ 
			0, b0c.allocMsg, 
			b0c.readMsg(buffer0.path); 
		]); 

		score.add([ 
			0, b1c.allocMsg, 
			b1c.readMsg(buffer1.path);
		]);

		score.add([ 
			0, synthMsg
		]);

		cleanupList.add({ 
			b0c.free; 
			b1c.free; 
		});
		
		^score.sort;
	}
}
