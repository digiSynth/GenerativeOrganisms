GenOrgBehavior : Hybrid {
	classvar instanceCount;
	var instanceNumber, synthDefName;

	initComposite {
		instanceNumber  = this.incrementInstanceCount;
		super.initComposite;
	}

	initHybrid { }

	incrementInstanceCount {
		if(instanceCount.notNil, {instanceCount = instanceCount + 1}, {instanceCount = 0});
		^instanceCount;
	}

	makeTemplates {
		//loads to modules as synthDef, parameters, envs.
		templater.behaviorSynthDef;
		templater.behaviorParameters;
		templater.behaviorEnvs;
	}

	parameters {
		^modules.parameters;
	}

	makeSynthDefs {
		//load the synthdef from the synthdef function defined by the module.
		modules[\synthDef] = modules.synthDef;
		super.makeSynthDefs;
	}

	tag { | tag, name |
		synthDefName = super.tag(tag, name)++instanceNumber.asString;
		^synthDefName;
	}

	free { this.class.removeAt(synthDefName); }

	play { | buffer, db(-12), outBus(0),
		target(server.defaultGroup), addAction(\addToTail) |
		Synth(
			modules.synthDef.name,
			this.getSynthArgs(db, outBus),
			target,
			addAction
		);
	}

	getSynthArgs { | db(-3), outBus(0) |
		var pars = modules.parameters.copy;
		pars[\ampDB] = db; pars[\out] = outBus;
		^pars.asPairs;
	}

	mutateWith { | target |
		var child = GenOrgBehavior.new(\mutation);
		var tmods = target.modules;
		child.modules.envs = this.mutateEnvs(tmods.envs);
		child.modules.parameters = this.mutateParameters(tmods.parameters);
		^child;
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
