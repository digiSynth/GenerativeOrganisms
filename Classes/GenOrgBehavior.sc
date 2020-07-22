GenOrgBehavior : CodexHybrid {
	classvar instances;
	var <>instance, <parameters, synthDefName;

	*initClass { instances = 0 }

	*notAt { | set | ^CodexComposite.notAt(set) }

	*newMutation { | set(\default) |
		^super.newCopyArgs(
			format("%_mutation", set).asSymbol
		).loadModules(set);
	}

	initComposite {
		instance = this.increment;
		this.evaluateModules;
		server = Server.default;
		this.processSynthDefs;
		this.initHybrid;
	}

	evaluateModules {
		/*modules.keysValuesDo({ | key, module |
		if(module.isFunction, {
		modules[key] = module.value;
		});
		});*/
		modules[\synthDef] = modules.synthDef;
	}

	name { ^(super.name.asString++instance).asSymbol }

	increment {
		instances = instances + 1;
		^(instances - 1);
	}

	*makeTemplates { | templater |
		templater.behaviorSynthDef;
		templater.behaviorArgs;
		templater.behaviorEnvs;
		templater.behaviorEnvsWrappers;
	}

	arguments { ^modules.behaviorArgs }

	getCurves {
		^this.arguments.collect({|item, index|
			[item, modules[item]];
		}).asPairs(Dictionary);
	}

	free { this.removeSynthDefs }

	playBehavior { | buffer, db(-12), outBus(0),
		target(server.defaultGroup), addAction(\addToTail) |
		Synth(
			modules.synthDef.name,
			this.getSynthArgs(db, outBus),
			target,
			addAction
		);
	}

	parameters_{ | newParameters |
		if(newParameters.isKindOf(GenOrgParameters), {
			parameters = newParameters;
		});
	}

	mutateWith { | target |
		var child = GenOrgBehavior.newMutation(moduleSet);
		var copyModules = this.mutateModules(target);
		copyModules.keysValuesDo({ | key, value |
			child[key] = value;
		});
		^child.initComposite;
	}

	mutateModules { | target |
		var tmodules = target.modules, child = ();
		child.add(\behaviorArgs -> this.mutateSpecs(tmodules));
		child.add(\behaviorEnvs -> this.mutateEnvs(tmodules));
		child.add(\behaviorEnvsWrappers ->
			modules.behaviorEnvWrappers.copy);
		child.add(\synthDef -> modules.synthDef.copy);
		^child;
	}

	mutateEnv { | env0, env1 |
		var levels = this.averageArr(env0.levels, env1.levels);
		var times = this.averageArr(env0.times, env1.times);
		var curves = this.averageArr(env0.curves, env1.curves);
		^Env(levels, times, curves);
	}

	mutateEnvs { | target |
		var tenvs = target.behaviorEnvs, child = ();
		modules.behaviorEnvs.keyValuesDo({ | key, value |
			child.add(key -> this.mutateEnv(value, tenvs[key]));
		});
		^child;
	}

	mutateSpecs { | target |
		var targs = target.behaviorArgs;
		var child = Dictionary.new;
		modules.behaviorArgs.keyValuesDo({ | key, value |
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

	getSynthArgs { | db(0), outbus(0) |
		var args = [];
		modules.args.keysValuesDo({ | key, value |
			 if(key==\timescale, {
				args = args.add([\timescale, value.map(1.0.rand)]);
			}, {
				var lo = format("%lo", key).asSymbol;
				var hi = format("%hi", key).asSymbol;
				args = args.add([lo, value.map(0.5.rand),
					hi, value.map(rrand(0.5, 1.0))]);
			});
		});
		^([\ampDB, db, \out, outbus]++args.flat.asPairs);
	}

}
