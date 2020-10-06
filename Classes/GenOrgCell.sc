GenOrgCell {
	var <buffer, <nucleus, <membrane, <gene, <>server;
	var freeList, cilia;

	*new { | buffer, nucleus, membrane, gene, server(Server.default) |
		^super.newCopyArgs(buffer, nucleus, membrane, gene, server);
	}

	initCell {
		freeList = List.new;
		freeList.add({ this.freeResources });
	}

	mate { | organism |
		if(organism.isKindOf(GenOrgCell), {
			var c_buffer = gene.mutate(buffer.value, organism.buffer.value);
			var c_nuclues = nucleus.mateWith(organism.nucleus);
			var c_membrane = GenOrgMembrane(membrane.moduleSet);
			var c_gene = GenOrgGene(gene.moduleSet);
			^GenOrgCell(c_buffer, c_nuclues, c_membrane, c_gene);
		});
		^nil;
	}

	eat { | organism |
		if(organism.isKindOf(GenOrgCell), {
			buffer = gene.mutate(buffer.value, organism.buffer.value);
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
		buffer.free;
	}

	free {
		freeList.do(_.value);
		freeList.clear;
		freeList.add({ this.freeResources });
	}

	onFree { | function | freeList.add(function) }
}
