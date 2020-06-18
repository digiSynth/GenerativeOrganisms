GenOrgBehavior : Hybrid { 
	classvar instanceCount;
	var instanceNumber, parameters; 

	initHybrid { 
		instanceNumber = this.class.incrementInstanceCount;
		parameters = GenOrgParameters.new;
	}

	incrementInstanceCount { 
		instanceCount = instanceCount ? 0 !? { instanceCount + 1 };
		^instanceCount;
	}

	makeTemplates { 
		templater.behaviorShell;
		this.arguments.do{ | item | 
			//These will be envelopes. They can be specified explicitly; but default they will be randomized with respect to their number of levels and the times.
			//I need to think about how to mate two sets of envelopes though.
			//I should also think about making MultiComposite and MultiHybrid so that if we are mating behaviors...or I can write a new behavior class (GenOrgBehaviorMutant) that doesn't rely on modules, but the loaded mutations of two modules. that doesn't rely on modules, but the loaded mutations of two modules...that will be easier.
			templater.behaviorEnv(item.asString);
		};
	}

	*arguments { 
		^[\rate, \pos, \amp, \ffreq, \impulseRate, \grainDur, \rq];
	}

	arguments { ^this.class.arguments; }

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
		tag = super.tag(tag, name); 
		^(tag++instanceNumber.asString);
	}

	play { | buffer, db(-12), outBus(0), 
		target(server.defaultGroup), addAction(\addToTail) | 
		Synth(
			modules.synthDef.name, 
			parameters.getSynthArgs(db, outBus), 
			target, 
			addAction
		);
	}


}

GenOrgBehaviorMutant {

	play {  }

}
