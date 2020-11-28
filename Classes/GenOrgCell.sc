GenOrgCell {
	var buffer, <nucleus, <membrane, <gene, <>server;
	var freeList, <cilia;

	*new { | buffer, nucleus, membrane, gene, server(Server.default) |
		^super.newCopyArgs(
			buffer,
			nucleus,
			membrane,
			gene,
			server
		).initCell;
	}

	asCreature { ^this.as(GenOrgCreature) }

	initCell {
		cilia ?? { cilia = IdentityDictionary.new.know_(true) }
		!? {
			cilia.asArray.do(_.free);
			cilia.clear;
		};

		case
		{ buffer.isKindOf(Ref) }{}
		{ buffer.isNil or: { buffer.value.isKindOf(Buffer).not } }{
			Error("No Buffer instance specified").throw
		}
		{ buffer.numChannels > 1 }{
			"Resetting buffer to mono.".warn;
			buffer = Buffer.readChannel(
				server,
				buffer.path,
				channels: 0
			);
		};
		freeList = List.new;
		freeList.add({ this.freeResources });
	}

	mateWith { | organism |
		var c_buffer = gene.mutate(buffer.value, organism.buffer.value);
		var c_nucleus = nucleus.mateWith(organism.nucleus);
		var c_membrane = GenOrgMembrane(membrane.moduleSet);
		var c_gene = GenOrgGene(gene.moduleSet)
		.folder_(gene.folder)
		.fileTemplate_(gene.fileTemplate);
		var cell = GenOrgCell(c_buffer, c_nucleus, c_membrane, c_gene);
		cilia !? {
			cilia.keysValuesArrayDo { | key, cilium |
				cell.addCilium(key, GenOrgCilium(cilium.moduleSet));
			};
		};
		^cell;
	}

	mutateWith { | organism |
		if(organism.isKindOf(GenOrgCell), {
			buffer = gene.mutate(this.buffer, organism.buffer);
		});
	}

	playCell { | timescale(1), out(0), target, addAction(\addToHead) |
		server.bind({
			membrane.output = out;
			membrane.playMembrane;
			target !? {
				membrane.synth.moveTo(target, addAction);
				cilia.asArray.do { | cilium |
					cilium.synth.moveBefore(membrane.synth);
				};
			};
			nucleus.playNucleus(
				buffer.value,
				timescale,
				membrane.input,
				membrane.synth,
				\addBefore
			);
			cilia.keysValuesDo { | key, value |
				membrane.synth.map(key, value.bus);
			};
		});
	}

	freeResources {
		membrane.free;
		nucleus.free;
		cilia !? {
			cilia.asArray.flat.do(_.free);
		};
		/*if(this.buffer.index.notNil, {
		this.buffer.free;
		buffer = nil;
		});*/
	}

	free {
		freeList.do(_.value);
		freeList.clear;
		freeList.add({ this.freeResources });
	}

	onFree { | function | freeList.add(function) }

	buffer { ^buffer.value }
	bufferRef { ^buffer }

	folder { ^gene.folder }
	folder_{ | newFolder | gene.folder = newFolder }

	fileTemplate { ^gene.fileTemplate }
	fileTemplate_{ | newTemplate | gene.fileTemplate = newTemplate }

	addCilium { | argument, cilium |
		if(cilium.isKindOf(GenOrgCilium)){
			cilia.add(argument -> cilium);
			server.bind({ cilium.makeSynth });
		};
	}

	detachCilium { | argument |
		if(cilia[argument].notNil){
			cilia.removeAt(argument).free;
		};
	}

	addAzimuth { | cilium |
		this.addCilium(\azimuth, cilium);
	}

	addElevation { | cilium |
		this.addCilium(\elevation, cilium);
	}

	addDistance { | cilium |
		this.addCilium(\distance, cilium);
	}
}
