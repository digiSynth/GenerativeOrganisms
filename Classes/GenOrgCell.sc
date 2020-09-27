GenOrgCell : GenOrgHybrid {
	var <>envs, <busses, synth;
	var membrane, cilium;

	*formatName { | symbol, key |
		var nKey = key.asString++"_"++UniqueID.next;
		^super.formatName(symbol, nKey);
	}

	buildSynthDef {
		modules.add(\synthDef -> SynthDef(\synth, {
			var buffer = \buffer.kr(0);
			var timescale = \timescsale.kr(1);
			var env = Env(
				[0, 1, 1, 0],
				[0.1, 1, 0.1].normalizeSum
			).kr(
				timeScale: timescale,
				doneAction: Done.freeSelf
			);
			var sig = modules.cellular_function(buffer, timescale);
			Out.ar(\out.kr(0), sig * env);
		}));
	}

	addSynthDef {
		this.generateEnvs;
		this.buildEnvGens;
		this.buildSynthDef;
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
						timeScale: \timescale.kr(1);
					)
				);
			}));
		});
	}

	generateEnvs {
		envs ?? {
			var tmpvar = ();
			modules.synhtDef.specs
			.keysValuesArrayDo({ | key, value |
				var numSegs = exprand(4, 32);
				tmpvar.add(key -> Env(
					Array.rand(numSegs, value.minval, value.maxval),
					Array.rand(numSegs - 1, 0.01, 1).normalizeSum,
					Array.rand(numSegs - 1, -12, 12)
				));
			});
			this.envs = tmpvar;
		}
	}

	envs_{ | newEnvs |
		if(envs.isKindOf(Dictionary), {
			envs = newEnvs;
			busses !? { busses.do(_.free) };
			envs.keys.do{ | key |
				busses.add(key -> Bus.control(server, 1));
			};
		});
	}

	playCell { | buffer, output(0), target(server.defaultGroup), addAction(\addToHead) |
		server.bind({
			var group = Group.new(target, addAction);
			synth = Synth(
				modules.synthDef.name,
				this.getArguments(buffer),
				target: group
			).register;
			envs.keys.do { | key |
				Synth(
					modules[key].name,
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

	getArguments { | buffer |
		var array = [];
		busses.keysValuesDo({ | key, value |
			array = array.add(key);
			array = array.add(value.asMap);
		});
		^(array++[\buffer, buffer]);
	}

	*makeTemplates { | templater |
		templater.cellular_function;
	}

	mutateWith { | target |
		if(moduleSet == target.moduleSet, {
			var child = GenOrgCell.basicNew(moduleSet);
			child.envs = this.mutateEnvs(target.envs);
			^child.setup;
		});
		^nil;
	}

	mutateEnvs { | targetEnvs |
		var newEnvs = ();
		envs.keysValuesDo({ | key, env |
			var target = targetEnvs[key];
			newEnvs.add(key -> Env(
				this.averageArray(env.levels, target.levels),
				this.averageArray(env.times, target.times),
				this.averageArray(env.curves, target.curves)
			))
		});
		^newEnvs;
	}

	averageArray { | array0, array1 |
		if(arr0.size!=arr1.size, { arr1 = arr1.resize(arr0) });
		^(arr0 + arr1 / 2);
	}

	mutateWith { | target |
		var child = GenOrgCell.mutation(moduleSet);
		this.mutateModules(target)
		.keysValuesDo({ | key, value |
			child[key] = value;
		});
		^child.initComposite;
	}

	mutateModules { | target |
		var tmodules = target.modules, child = ();
		child.add(\args -> this.mutateArgs(tmodules));
		child.add(\envs -> this.mutateEnvs(tmodules));
		child.add(\wrappers -> modules.wrappers.copy);
		child.add(\synthDefFunction -> modules.synthDefFunction.copy);
		^child;
	}

	mutateEnv { | env0, env1 |
		var levels = this.averageArr(env0.levels, env1.levels);
		var times = this.averageArr(env0.times, env1.times);
		var curves = this.averageArr(env0.curves, env1.curves);
		^Env(levels, times, curves);
	}

	mutateEnvs { | target |
		var tenvs = target.cellularEnvs, child = ();
		modules.envs.keyValuesDo({ | key, value |
			child.add(key -> this.mutateEnv(value, tenvs[key]));
		});
		^child;
	}

	mutateSpecs { | target |
		var targs = target.cellularArgs;
		var child = Dictionary.new;
		modules.args.keyValuesDo({ | key, value |
			var tval = targs[key];
			var minval = value.minval + tval.minval / 2;
			var maxval = value.maxval + tval.maxval / 2;
			var warp = [value.warp, tval.warp].choose;
			child.add(key -> ControlSpec(minval, maxval, warp));
		});
	}

	averageArr { | arr0, arr1 |
		if(arr0.size!=arr1.size, { arr1 = arr1.resize(arr0) });
		^(arr0 + arr1 / 2);
	}

	getSynthArgs { | buffer, db(0), outbus(0) |
		var args = [];
		modules.args.keysValuesDo({ | key, value |
			if(key==\timescale, {
				args = args.add(\timescale); args = args.add(value.map(1.0.rand));
			}, {
				var lo = format("%lo", key).asSymbol;
				var hi = format("%hi", key).asSymbol;
				args = args.add(lo);
				args = args.add(value.map(0.5.rand));
				args = args.add(hi);
				args = args.add(rrand(0.5, 1.0));
			});
		});
		^([\buf, buffer, \ampDB, db, \out, outbus]++args);
	}

	membrane_{ | newMembrane |
		membrane !? { membrane.free };
		if(newMembrane.isKindOf(GenOrgMembrane), {
			membrane = newMembrane;
			membrane.onFree({ membrane = nil });
		});
		this.attachCilium;
	}

	cilium_{ | newCilium |
		cilium !? { cilium.free };
		if(newCilium.isKindOf(GenOrgCilium), {
			cilium = newCilium;
		});
		this.attachCilium;
	}

	attachCilium {
		if(cilium.notNil and: { membrane.notNil }, {
			//membrane.attachCilium(cilium);
		});
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
