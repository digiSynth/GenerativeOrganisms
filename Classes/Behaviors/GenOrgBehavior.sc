GenOrgBehavior : Hybrid {
	classvar <instanceCount;
	var <instanceNumber;

	initComposite {
		instanceNumber = this.class.increment;
		super.initComposite;
	}

	*increment {
		instanceCount = instanceCount !? { instanceCount + 1 } ?? { 0 };
		^instanceCount;
	}

	makeTemplates {
		//loads to modules as synthDef, parameters, envs.
		templater.behaviorSynthDef;
		templater.behaviorParameters;
		templater.behaviorEnvs;
	}

	makeSynthDefs {
		//load the synthdef from the synthdef function defined by the module.
		modules[\synthDef] = modules.synthDef;
		super.makeSynthDefs;
	}

	formatName { | string | ^(super.formatName(string)++instanceNumber); }

	free { this.class.removeAt(modules.synthDef.name); }

	play { | buffer, db(-12), outBus(0),
		target(server.defaultGroup), addAction(\addToTail) |
		Synth(
			modules.synthDef.name,
			this.getSynthArgs(buffer, db, outBus),
			target,
			addAction
		);
	}

	getSynthArgs { | buffer, db(-3), timescale(1.0), outBus(0) |
		var pars = modules.parameters.copy;
		var arr = pars.keys.asArray.collect({ | key |
			var  item = pars[key];
			var lo = this.tag(key, \lo);
			var hi = this.tag(key, \hi);
			[[lo.asSymbol, item.map(0.5.rand)],
				[hi.asSymbol, item.map(0.5.rand+0.5.rand)]];
		});
		arr = arr.add([\buf, buffer, \ampDB, db,
			\out, outBus, \timescale, timescale]);
		^arr.flat;
	}

	mutateWith { | target |
		var tmods = target.modules, mutation = ();
		mutation.envs = this.mutateEnvs(tmods.envs);
		mutation.parameters = this.mutateParameters(tmods.parameters);
		^GenOrgBehavior.basicNew
		.getModules(input:mutation)
		.initComposite;
	}

	getModules { | from, input |
		super.getModules(from);
		input !? {
			input.keysDo({ | key | modules[key] = input[key] });
		};
	}

	mutateEnvs { | target |
		var mutation = ();
		modules.envs.keysValuesArrayDo { | key, value |
			var tarItem = target[key];
			var levels = this.averageArr(value.levels, tarItem.levels);
			var times = this.averageArr(value.times, tarItem.times);
			var curves = this.averageArr(value.curves, tarItem.curves);
			mutation[key] = Env(levels, times, curves);
		};
		^mutation;
	}

	averageArr { | arr0, arr1 |
		if(arr0.size != arr1.size, {
			arr1 = arr1.resize(arr0);
		});
		^(arr0 + arr1 / 2);
	}

	mutateParameters { | target |
		var mutataion = ();
		modules.parameters.keysValuesDo({ | key, item |
			var tarItem = target[key];
			mutataion[key] = [
				item.minval + tarItem.minval / 2,
				item.maxval + tarItem.maxval / 2;
				[item.warp + tarItem.warp].choose;
			].asSpec;
		});
		^mutataion;
	}
}