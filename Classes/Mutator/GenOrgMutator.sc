GenOrgMutator : Hybrid { 
	var incrementer, <options, cleanupList; 

	makeTemplates { 
		templater.mutator;
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
		^((Synth.basicNew(modules.mutator.name)).newMsg(
			args: this.processSynthArgs(
				buffer0, 
				buffer1, 
				timescale
			);
		));
	}

	processSynthArgs { | buffer0, buffer1, timescale | 
		var dict = modules.mutator.metadata; 
		dict.keysDo({ | key | 
			dict[key]  = dict[key].map(1.0.rand); 
		});
		^this.enforceArgs(dict.asPairs, buffer0, buffer1, timescale);
	}

	enforceArgs { | argArray, buffer0, buffer1, timescale |
		^this.enforceTimescale( 
			this.enforceBufs(argArr, buffer0, buffer1),
			timescale
		);
	}

	enforceBufs { | argArr, buffer0, buffer1 |
		^this.addArg( 
			this.addArg(argArr, \buf0, buffer0), 
			\buf1, 
			buffer1
		);
	}

	enforceTimescale { | argArray, timescale(1)|
		^this.addArg(argArr, \timescale, timescale);
	}

	addArg { |argArray, key, val| 
		^if(argArray.containsIdentical(key).not, {
			argArray++[key, val];		
		}, {argArray});
	}

	render { | buffer0, buffer1, timescale, action| 
		var oscpath = PathName.tmp +/+ UniqueID.next ++ ".osc";
		var outpath = incrementer.increment;
		this.getScore(buffer, buffer1, timescale).recordNRT(
			oscpath, 
			outpath, 
			nil, 
			48e3, 
			"wav", 
			"int24", 
			options, 
			"", 
			duration, 
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
