GenOrg : Codex {
	classvar <>mutations;
	var server, nrtServer;

	var <>buffer, <>bus = 0, <>server;
	var player, spatializer, renderer;
	var <voicingGenes, <matingGenes, <eatingGenes;

	var <>mutations;

	*makeTemplates { | templater |
		templater.genOrgVoicer("voicingFunc");
		templater.genOrgSpatializer("spatialFunc");
		templater.genOrgMutator("eatingFunc");
		templater.genOrgMutator("matingFunc");
	}

	// *basicNew is like *new but does not call initCodex
	// For use when mating organisms
	*basicNew { | moduleSet, from |
		^super.newCopyArgs(
			moduleSet ?? { Error("No module set specified").throw }
		).getModules(from);
	}

	initCodex {

		server = server ? Server.default;

		modules.make {
			var mutationDef;

			if (~voicing.isNil || ~mating.isNil || ~eating.isNil) {
				var cachedModules = this.class.cache[ this.moduleSet ];

				~voicing = cachedModules.use {
					~voicing = SynthDef('voicing', {
						var buffer = \buffer.kr(999);
						var x, y, z;
						var sig;

						x = \x.kr(0, spec: ControlSpec(-1.0, 1.0));
						y = \y.kr(0, spec: ControlSpec(-1.0, 1.0));

						sig = ~voicingFunc.value(modules, buffer);
						sig = ~spatialFunc.value(
							modules,
							sig,
							\x.kr(0, spec: ControlSpec(-1.0, 1.0)),
							\y.kr(0, spec: ControlSpec(-1.0, 1.0)),
							\z.kr(0, spec: ControlSpec(-1.0, 1.0))
						);
						OffsetOut.ar(
							\out.kr(0),
							sig
						);
					});
				};

				mutationDef = { | function |
					var sig = function.value(
						modules,
						\buffer0.kr(998),
						\buffer1.kr(999)
					);
					var env = Env(
						[ 0, 1, 1, 0 ],
						[ 1, 100, 1 ].normalizeSum,
						\welch
					).ar(Done.freeSelf);

					sig * env;
				};

				~mating = cachedModules.use {
					~mating = SynthDef('mating', {
						Out.ar(\out.kr(0), mutationDef.value(~matingFunc));
					});
				};

				~eating = cachedModules.use {
					~eating = SynthDef('eating', {
						Out.ar(\out.kr(0), mutationDef.value(~eatingFunc));
					});
				};
			};

			voicingGenes = voicingGenes ? GenOrgChromosome(server, ~voicing);
			matingGenes = matingGenes ? GenOrgChromosome(server, ~mating);
			eatingGenes = eatingGenes ? GenOrgChromosome(server, ~eating);
		};

		nrtServer = Server.new(
			("GenOrg-NRT-server"++UniqueID.next).asSymbol,
			options: ServerOptions.new
			.numInputBusChannels_(0)
			.numOutputBusChannels_(1)
			.verbosity_(-2)
			.sampleRate_(48e3)
		);
	}

	mate { | organism, action |
		var newOrg = GenOrg.basicNew(this.moduleSet);

		newOrg.matingGenes = matingGenes.mutateWith(organism.matingGenes);
		newOrg.voicingGenes = voicingGenes.mutateWith(organism.voicingGenes);
		newOrg.eatingGenes = eatingGenes.mutateWith(organism.eatingGenes);

		this.mutateBuffers(
			organism.buffer,
			modules.mating,
			matingGenes,
			{ | buffer | newOrg.buffer = buffer; action.value }
		);

		^newOrg.initCodex;
	}

	eat { | organism |
		this.mutateBuffers(
			organism.buffer,
			modules.eating,
			eatingGenes,
			{ | buffer | this.buffer = buffer }
		);
		organism.free;
	}

	free {
		nrtServer.remove;
		buffer.free;
	}

	kill { | organism | try { organism.free } }

	mutateBuffers { | buffer, synthDef, genes, action |
		var oscFile, output;
		var thisBuffer, thatBuffer;
		var score = Score.new;

		thisBuffer = Buffer.new(server, server.sampleRate, 1);
		score = score.add([
			0, thisBuffer.allocMsg,
			thisBuffer.readMsg(this.buffer.path);
		]);

		thatBuffer = Buffer.new(server, server.sampleRate, 1);
		score = score.add([
			0, thatBuffer.allocMsg,
			thatBuffer.readMsg(buffer.path);
		]);

		score = score.add([ 0, [ \d_recv, synthDef.asBytes ] ]);
		score = score.add([
			0, Synth.basicNew(synthDef.name, nrtServer)
			.newMsg(args: genes.expressGenes
				++ [ \buffer0, thisBuffer, \buffer1, thatBuffer ]
			);
		]);

		score = score.add([ 1.0, 0 ]);
		score.sort;

		oscFile = PathName.tmp +/+ UniqueID.next ++".osc";

		output = this.mutations ? this.class.mutations ?? {
			var f = PathName.tmp
			+/+ "GenOrg-Mutations"
			+/+ Date.getDate.format("%Y-%m-%d");

			this.class.mutations = this.mutations = f;
			f;
		};
		output = output.mkdir +/+ UniqueID.next ++ ".wav";

		score.recordNRT(
			oscFile,
			output,
			headerFormat: "wav",
			sampleFormat: "int24",
			options: nrtServer.options,
			action: { fork {

				File.delete(oscFile);

				Buffer.read(server, output, action: { | buf |
					action.value(buf.normalize);
				});
			} }
		);
	}

	resound { | target, addAction('addToHead') |
		var args = voicingGenes.expressGenes;
		args = args.add('out');
		args = args.add(bus);

		args = args.add('buffer');
		args = args.add(buffer);

		^Synth(modules.voicing.name, args, target, addAction);
	}

	voicingGenes_{ | newGenes |
		if (newGenes.isKindOf(GenOrgChromosome)) {
			voicingGenes = newGenes;
		}
	}

	matingGenes_{ | newGenes |
		if (newGenes.isKindOf(GenOrgChromosome)) {
			matingGenes = newGenes;
		}
	}

	eatingGenes_{ | newGenes |
		if (newGenes.isKindOf(GenOrgChromosome)) {
			eatingGenes = newGenes;
		}
	}
}

GenOrgChromosome : IdentityDictionary {
	var <>server, <genes;

	*new { | server, synthDef |
		^super.new
		.know_(true)
		.server_(server ? Server.default)
		.buildGenesFrom(synthDef);
	}

	buildGenesFrom { | synthDef |
		var buildFunc;

		synthDef ?? { ^this };

		buildFunc = {
			synthDef.desc.specs.keysValuesDo { | key, spec |
				this.add(key -> GenOrgGene(spec))
			};
		};

		buildFunc.try {
			fork {
				server.sync;
				buildFunc.value
			}
		};
	}

	expressGenes {
		var arr = [];
		this.keysValuesDo { | key, gene |
			arr = arr.add(key);
			arr = arr.add(gene.express);
		};
		^arr;
	}

	mutateWith { | chromosome |
		var newGenes;
		var thisGene, thatGene;

		if (chromosome.isKindOf(GenOrgChromosome).not) {
			^nil
		};

		newGenes = GenOrgChromosome.new(server);

		// Create genes from the intersection of two sets
		(this.keys & chromosome.keys).do { | key |
			thisGene = this.at(key);
			thatGene = chromosome.at(key);
			newGenes.add(key -> GenOrgGene(
				thatGene.spec,
				rrand(thisGene.lo, thatGene.lo),
				rrand(thisGene.hi, thatGene.hi)
			));
		};

		// Add any difference genes to the mix
		(this.keys -- chromosome.keys).do { | key |
			thisGene = this.at(key) ? chromosome.at(key);
			newGenes.add(key -> GenOrgGene(
				thisGene.spec,
				thisGene.lo,
				thisGene.hi
			));
		};

		^newGenes;
	}

	randomizeGenes {
		this.asArray.do(_.randomize);
	}

	storeItemsOn { arg stream, itemsPerLine = 5;
		var itemsPerLinem1 = itemsPerLine - 1;
		var last = this.size - 1;
		this.associationsDo({ arg item, i;
			item.key.storeOn(stream);
			if (i < last, { stream.comma.space;
				if (i % itemsPerLine == itemsPerLinem1, { stream.nl.space.space });
			});
		});
	}

	printItemsOn { arg stream, itemsPerLine = 5;
		var itemsPerLinem1 = itemsPerLine - 1;
		var last = this.size - 1;
		this.associationsDo({ arg item, i;
			item.key.printOn(stream);
			if (i < last, { stream.comma.space;
				if (i % itemsPerLine == itemsPerLinem1, { stream.nl.space.space });
			});
		});
	}

	put { | key, value |
		if (value.isKindOf(GenOrgGene).not) {
			Error("% can only store objects of kind %".format(
				this.class, GenOrgGene
			)).throw;
		};

		super.put(key, value);
	}
}

GenOrgGene {
	var <>spec, <>lo, <>hi, <value;

	*new { | spec, lo, hi |
		var obj = super.newCopyArgs(
			spec ? ControlSpec(),
			lo ? 1.0.rand,
			hi ? 1.0.rand
		);
		obj.randomize;
		^obj;
	}

	randomize {
		value = 1.0.rand;
		^this.express;
	}

	value_{ | newValue |
		value = newValue.wrap(0.0, 1.0);
	}

	express { ^spec.map(value.linlin(0.0, 1.0, lo, hi)) }
}