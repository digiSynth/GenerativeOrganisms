GenOrgNucleus : GenOrgHybrid {
	var <envs, <busses, synth;

	*contribute { | versions |
		var path = Main.packages.asDict.at(\GenOrg)
		+/+"Classes/GenOrgNucleus";

		versions.add(
			[\tgrains, path+/+"tgrains"]
		);

		versions.add(
			[\playbuf, path+/+"tgrains"]
		);
	}

	*formatName { | symbol, key |
		var nKey = key.asString++"_"++UniqueID.next;
		^super.formatName(symbol, nKey);
	}

	buildSynthDef {
		modules.add(\synthDef -> SynthDef(\synth, {
			var buffer = \buffer.kr(0);
			var timescale = \timescale.kr(2);
			var env = Env(
				[0, 1, 1, 0],
				[0.1, 1, 0.1].normalizeSum
			).kr(
				timeScale: timescale,
				doneAction: Done.freeSelf
			);
			var sig = modules.nucleus_function(buffer, timescale);
			Out.ar(\out.kr(0), sig * env);
		}));
	}

	addSynthDef {
		this.buildSynthDef;
		this.generateEnvs;
		this.buildEnvGens;
		this.class.processSynthDefs(modules, moduleSet);
	}

	*findSynthDefs { | modules |
		^modules.select({ | module |
			module.isKindOf(SynthDef);
		}).asArray;
	}

	*namedSynthDefs { | modules, key |
		var synthDefs = this.findSynthDefs(modules);
		synthDefs.do { | synthDef |
			synthDef.name = this.formatName(synthDef.name, key);
		};
		^synthDefs;
	}

	*processSynthDefs { | modules, key |
		processor.add(this.namedSynthDefs(modules, key));
	}

	*removeSynthDefs { | modules |
		processor.remove(this.findSynthDefs(modules));
	}

	buildEnvGens {
		envs.keysValuesDo({ | key, env |
			modules.add(key -> SynthDef(key, {
				Out.kr(
					busses[key],
					env.kr(
						doneAction: Done.freeSelf,
						timeScale: \timescale.ir(1);
					)
				);
			}));
		});
	}

	generateEnvs {
		if(envs.isNil or: { envs.isEmpty }, {
			var dict = ();
			modules.synthDef.specs.keysValuesDo({ | key, value |
				var numSegs = exprand(4, 32);
				dict.add(key -> Env(
					Array.rand(numSegs, value.minval, value.maxval),
					Array.rand(numSegs - 1, 0.01, 1).normalizeSum,
					Array.rand(numSegs - 1, -12, 12)
				));
			});
			this.envs = dict;
		});
	}

	envs_{ | newEnvs |
		if(newEnvs.isKindOf(Dictionary), {
			envs = newEnvs;
			busses !? { busses.do(_.free) };
			busses = ();
			envs.keys.do{ | key |
				busses.add(key -> Bus.control(server, 1));
			};
		});
	}

	playNucleus { | buffer, timescale(1), out(0), target(server.defaultGroup), addAction(\addToHead) |
		server.bind({
			var group = Group.new(target, addAction);
			synth = Synth(
				modules.synthDef.name,
				this.getArguments(buffer, timescale, out),
				target: group
			).register;
			envs.keys.do { | key |
				Synth(
					modules[key].name,
					[ \timescale, timescale ],
					target: group,
					addAction: \addToHead
				);
			};
			synth.onFree({
				group.free;
			});
		});
	}

	freeResources {
		busses.asArray.do(_.free);
		this.class.removeSynthDefs(modules);
	}

	free {
		if(synth.isPlaying, {
			synth.onFree({ this.freeResources });
		}, { this.freeResources });
	}

	getArguments { | buffer, timescale, out |
		var array = [];
		busses.keysValuesDo({ | key, value |
			array = array.add(key);
			array = array.add(value.asMap);
		});
		^(array++[\buffer, buffer, \timescale, timescale, \out, out]);
	}

	*makeTemplates { | templater |
		templater.nucleus_function;
	}

	mateWith { | target |
		var child = GenOrgNucleus.basicNew(moduleSet);
		if(moduleSet != target.moduleSet, {
			target.moduleSet = moduleSet;
		});
		child.envs = this.mutateEnvs(target.envs);
		^child.setup;
	}

	mutateEnvs { | targetEnvs |
		var newEnvs = ();
		envs.keysValuesDo({ | key, env |
			var target = targetEnvs[key];
			var toAdd = env + target / 2;
			toAdd.times = toAdd.times.normalizeSum;
			newEnvs.add(key -> toAdd);
		});
		^newEnvs;
	}

	moduleSet_{ | newSet, from |
		this.class.removeSynthDefs(modules);
		super.moduleSet_(newSet, from);
	}

	reloadScripts {
		this.class.removeSynthDefs(modules);
		cache.removeModules(this.class.name, moduleSet);
		this.moduleSet = moduleSet;
	}
}
