GenOrgCilium : GenOrgHybrid {
	var <bus, <synth;

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
			Out.kr(\out.kr(0), sig);
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
		synth = Synth(
			modules.synthDef.name,
			[ \out, bus ]
		).register;
	}

	initGenOrgHybrid {
		this.makeBus;
	}

	free {
		if(synth.isPlaying){ synth.free };
	}

	moduleSet_{ | newSet, from |
		synth.free;
		super.moduleSet_(newSet, from);
	}
}
