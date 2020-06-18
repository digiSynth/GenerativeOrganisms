GenOrgBehavior : Hybrid { 
	classvar instanceCount;
	var instanceNumber, <parameters, tag; 

	initComposite { 
		instanceNumber  = this.incrementInstanceCount; 
		super.initComposite;
	}

	initHybrid { 
		parameters ?? {parameters = GenOrgParameters.new(moduleSet)};
	}

	incrementInstanceCount { 
		instanceCount = instanceCount ? 0 !? { instanceCount + 1 };
		^instanceCount;
	}

	makeTemplates { 
		templater.behaviorShell;
		this.arguments.do{ | item | 
			templater.behaviorEnv(item.asString);
		};
	}

	arguments { 
		^[\rate, \pos, \amp, \ffreq, \impulseRate, \grainDur, \rq];
	}

	makeSynthDefs { 
		this.generateSynthDef; 
		this.class.processSynthDefs(modules.synthDef);
	}

	generateSynthDef { 
		var synthDef = this.checkModules(
			modules.behaviorShell(this.getCurves(block))
		); 
		modules.add(\synthDef -> synthDef);
	}

	getCurves { 
		^this.arguments.collect({|item, index| 
			[item, modules[item].value(block[index])]
		}).asPairs(Dictionary);
	}

	tag { | tag, name | 
		tag = super.tag(tag, name)++instanceNumber.asString; 
		^tag;
	}

	getTag { ^this.tag.asSymbol; }

	free { this.class.removeAt(this.getTag); }

	play { | buffer, db(-12), outBus(0), 
		target(server.defaultGroup), addAction(\addToTail) | 
		Synth(
			modules.synthDef.name, 
			parameters.getSynthArgs(db, outBus), 
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
		var child = GenOrgBehavior.basicNew(\mutation);
		this.mutateModules(target.modules).keysValuesDo({ 
			| key, value |
			child.modules[key] = value;
		});
		child.parameters = parameters.mutateWith(target.parameters);
		^child.initComposite;
	}

	mutateModules { | targetModules | 
		var newModules = ();
		this.arguments.do{ | key | 
			newModules.add(key -> this.mutateEnv(
				modules[key], targetModules[key]
			));
		}; 
		^newModules;
	}

	mutateEnv { | env0, env1 | 
		var levels = this.averageArr(env0.levels, env1.levels); 
		var times = this.averageArr(env0.times, env1.times); 
		var curves = this.averageArr(env0.curves, env1.curves);
		^Env(levels, times, curves);
	}

	averageArr { | arr0, arr1 | 
		if(arr0.size != arr1.size, { 
			arr1 = arr1.resize(arr0);
		}); 
		^(arr0 + arr1 / 2);
	}
}
