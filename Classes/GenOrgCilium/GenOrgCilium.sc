GenOrgCilium : GenOrgHybrid {
	var bus, synth, membrane;

	*contribute { | versions |
		var path = Main.packages.asDict.at(\GenOrg)
		+/+"Classes/GenOrgCilium";

		versions.add(
			[\lfnoise2, path+/+"lfnoise2"]
		);

		versions.add(
			[\fm, path+/+"fm"]
		);
	}

	makeTemplates { | templater |
		templater.cilia_function;
	}

	buildSynthDef {
		^SynthDef(\synth, {
			var sig = modules.cilia_function.value
			.range(
				\lo.kr(0),
				\hi.kr(1)
			);
			Out.ar(\out.kr(0), sig);
		});
	}

	makeBus {
		bus.free;
		bus = Bus.control(server, 1);
	}

	makeSynth {
		synth !? {
			if(synth.isPlaying, { synth.free });
		};
		synth = Synth(modules.synthDef.name, [
			\out, bus
		]).register;
		synth.onFree({ this.detachCilium });
	}

	initGenOrgHybrid {
		this.makeBus;
		this.makeSynth;
	}

	attachTo { | input, argument |
		if(input.isKindOf(GenOrgMembrane), {
			membrane = input;
			membrane.synth.map(argument, bus);
		});
	}

	detachCilium {
		if(membrane.notNil and: { synth.isPlaying }, {
			synth.free;
			this.makeSynth;
		});
	}

	moduleSet_{ | newSet, from |
		synth.free;
		super.moduleSet_(newSet, from);
	}
}
