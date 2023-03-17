/*
GenOrgSimulation is, as its name suggests, supposed to run a simulation of GenOrgs within it, doing so automatically over time.

It should probably have some kind of widget or window for displaying data.

It should be configured by defining and adding GenOrgSpecies to a list. The parameters of the GenOrg species instances -- how long the organisms live, who they can mate with, who they can eat, etc -- will determine the general shape of the simulation.

The simulation will be organized in turns using a simple implementation of a game loop.
*/
GenOrgSimulation { }

/*
GenOrgSpecies is a factory for deploying GenOrgCritters. It should allow the user to specify high-level behaviors and attributes that will apply to every critter stored within a List.

Some behaviors might involve making sure that critters can't mate with their relatives for instance, how often they have to eat, when they can mate, and how long they can live.

Much of this should be specified as ranges so that, for example, not every critter dies of old age at the same age.
*/
GenOrgSpecies {
	var <>lifespan;

	var <>moduleSet;
	var critters;

	*new { | moduleSet |
		^super.newCopyArgs(moduleSet).initSpecies;
	}

	initSpecies {
		critters = critters ? List.new;
	}

	*moduleSets { ^GenOrg.moduleSets }

	addCritter { | buffer |
		var critter = GenOrgCritter(this.moduleSet)
		.buffer_(buffer);

		critters.add(critter);
	}
}

/*
GenOrgCritter extends the project's metaphor of the organism, establishing "living" traits for GenOrg objects. Notably, it does not inherit from GenOrg but instead stores and operates on instances of it.

The design of this class is similar to what you would expect from a video game character. It will have health, hit points, an age, and an update function, which will advance these traits across "turns."

The critter should also store a linked-list that describes its family tree.

There is an open question about whether or not GenOrgCritter should be able to store a function or a pattern or another type of routine that will allow it to perform client-side sequencing of the GenOrg instance it stores. If the answer is no, it should simply be responsible for calling GenOrg's "resound" method at the appropriate time.

Depending on the answer to the above question, one should consider whether GenOrgCritter should/could hold many instances of GenOrg, each with independent buffers (but defined with the same moduleSet), in order to create even more complex sonic behaviors.

If it can hold many GenOrg objects within it, I would consider renaming the GenOrg class to GenOrgCell because that would be in better keeping with the metaphor at play. I would consider doing this -- in fact I already have, but I would probably keep its name the same in order to establish a practice by which a quark that uses Codex is named after the class within it that inherits from Codex. Conversely, because I am set on calling this quark GenOrg, I should call the central Codex-based class GenOrg as well.
*/
GenOrgCritter {
	var <>moduleSet;
	var org;

	*new { | moduleSet |
		^super.newCopyArgs(moduleSet).addOrg;
	}

	addOrg {
		org !? { org.free };
		org = GenOrg(this.moduleSet);
	}
}

GenOrg : Codex {
	classvar mutations = nil;
	var server, nrtServer;

	var <>buffer, <>bus = 0, <>server;
	var player, spatializer, renderer;
	var <voicingGenes, <matingGenes, <eatingGenes;

	var >mutations;

	var killIt = false, <isPlaying = false, isRendering = false;

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

	*mutations {
		mutations ?? {
			mutations = PathName.tmp
			+/+ "GenOrg-Mutations"
			+/+ Date.getDate.format("%Y-%m-%d");
		};
		^mutations;
	}

	mutations {
		mutations ?? { mutations = this.class.mutations }
		^mutations;
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
		var newOrg;

		if (this.hasBuffer.not || organism.hasBuffer.not) {
			"WARNING: Tried to mate without buffers".postln;
			^nil
		};

		newOrg = GenOrg.basicNew(this.moduleSet);

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

	canFree { ^(isPlaying.not && isRendering.not) }

	free {
		if (this.canFree) {
			nrtServer.remove;
			buffer.free;
			buffer = nil;
		} /* else */ {
			killIt = true;
		};
	}

	hasBuffer {
		^(buffer.notNil && (try { buffer.numFrames > 0 } { false }));
	}

	kill { | organism | try { organism.free } }

	mutateBuffers { | buffer, synthDef, genes, action |
		if (isRendering) {
			fork {
				while { isRendering } {
					server.sync;
				};
				this.prMutate(buffer, synthDef, genes, action);
			}
		} /* else */ {
			this.prMutate(buffer, synthDef, genes, action);
		};
	}

	prMutate { | buffer, synthDef, genes, action |
		var oscFile, output;
		var thisBuffer, thatBuffer;
		var score = Score.new;

		isRendering = true;

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

		output = this.mutations.mkdir;
		output = output +/+ UniqueID.id.next ++ ".wav";

		score.recordNRT(
			oscFile,
			output,
			headerFormat: "wav",
			sampleFormat: "int24",
			options: nrtServer.options,
			action: {
				isRendering = false;
				fork {

					File.delete(oscFile);

					Buffer.read(server, output, action: { | buf |
						action.value(buf.normalize);
					});

					if (killIt) {
						this.free;
					};
				};
			}
		);
	}

	resound { | target, addAction('addToHead') |
		var genes, synth;

		this.hasBuffer ?? { ^nil };

		isPlaying = true;

		genes = voicingGenes.expressGenes;
		genes = genes.add('out');
		genes = genes.add(bus);

		genes = genes.add('buffer');
		genes = genes.add(buffer);

		synth = Synth(
			modules.voicing.name,
			genes,
			target,
			addAction
		);

		synth.register;
		synth.onFree {
			isPlaying = false;
			if (killIt) {
				this.free;
			};
		};
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
			// ^nil
			^this.deepCopy;
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

	storeItemsOn { arg stream, itemsPerLine = 10;
		var itemsPerLinem1 = itemsPerLine - 1;
		var last = this.size - 1;
		this.associationsDo({ arg item, i;
			item.key.storeOn(stream);
			if (i < last, { stream.comma.space;
				if (i % itemsPerLine == itemsPerLinem1, { stream.nl.space.space });
			});
		});
	}

	printItemsOn { arg stream, itemsPerLine = 10;
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