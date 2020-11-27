GenOrgCell {
	var buffer, <nucleus, <membrane, <gene, <>server;
	var freeList, cilia;

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
		var c_nuclues = nucleus.mateWith(organism.nucleus);
		var c_membrane = GenOrgMembrane(membrane.moduleSet);
		var c_gene = GenOrgGene(gene.moduleSet)
		.folder_(gene.folder)
		.fileTemplate_(gene.fileTemplate);
		^GenOrgCell(c_buffer, c_nuclues, c_membrane, c_gene);
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
			};
			nucleus.playNucleus(
				buffer.value,
				timescale,
				membrane.input,
				membrane.synth,
				\addBefore
			);
		});
	}

	freeResources {
		membrane.free;
		nucleus.free;
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
}
