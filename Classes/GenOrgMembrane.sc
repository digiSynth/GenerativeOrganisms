GenOrgMembrane : CodexHybrid {
	var freeFunctions, isFreed = false;
	var <group, <inputBus, <outputBus, <synth;
	var <lag, <azimuth, <elevation, <distance;
	var pauser, cilium;

	*makeTemplates { | templater |
		templater.membraneFunction;
		this.setMembrane(templater);
	}

	*setMembrane { | templater | this.subclassResponsibility(thisMethod) }

	initComposite {
		this.addSynthDef;
		super.initComposite;
	}

	initHybrid { this.onFree }

	addSynthDef {
		modules[\synthDef] ?? {
			var synthDef = modules.function(modules[\membraneWrap]);
			modules.add(\synthDef -> synthDef);
		};
	}

	initResources {
		this.makeBusses;
		this.initGroup;
	}

	makeBusses {
		inputBus ?? {inputBus = Bus.audio(server, 1)};
		outputBus ?? {outputBus  =  0};
	}

	inputBus_{ | newBus |
		if(inputBus != newBus, {
			inputBus = newBus;
			if(synth.isPlaying, { synth.set(\in, inputBus) })
		});
	}

	outputBus_{ | newBus |
		if(outputBus != newBus, {
			outputBus = newBus;
			if(synth.isPlaying, { synth.set(\out, outputBus) })
		});
	}

	initGroup { group = Group.new }

	initSynth {
		this.initResources;
		lag = lag ?? {server.latency  * 0.1};
		synth = Synth.newPaused(modules.synthDef.name, [
			\in, inputBus,
			\out, outputBus,
			\angle, pi/2,
			\lag, lag,
			\timer, server.latency,
			\doneAction, 0
		], group).register;
		synth.onFree({ this.freeList });
	}

	freeList { freeFunctions.do(_.value) }

	onFree { | function |
		freeFunctions ?? {
			var list = List.new;
			list.add({ this.freeResources });
			freeFunctions = list;
		};
		function !? { freeFunctions.add(function) };
	}

	isRunning { synth !? {^synth.isRunning} ?? {^false} }

	isPlaying { ^synth.isPlaying }

	freeResources {
		group.free;
		this.freeBus(inputBus);
		this.freeBus(outputBus);
		inputBus = outputBus = nil;
	}

	freeBus { | bus |
		if(bus.isKindOf(Bus) and: {bus.index.notNil}, {
			bus.free;
		});
	}

	free {
		if(this.isPlaying, {
			if(this.isRunning, {
				synth.set(\doneAction, 2, \gate, 0);
			}, { synth.free });
		}, { this.freeList });
	}

	setArg { | key, value |
		if({this.isPlaying}, {
			synth.set(key, value);
		});
	}

	azimuth_{ | newAzimuth(pi) |
		azimuth = newAzimuth.wrap(pi.neg, pi);
		this.setArg(\azimuth, azimuth);
	}

	elevation_{ | newElevation(0) |
		elevation = newElevation.wrap(pi.neg, pi);
		this.setArg(\elevation, elevation);
	}

	distance_{ | newDistance(2) |
		distance = newDistance.clip(1.0, 100.0);
		this.setArg(\distance, distance);
	}

	lag_{ |newLag(server.latency)|
		lag = newLag;
		this.setArg(\lag, lag);
	}

	awaken { | doneAction(0) |
		if(this.isRunning.not, {
			if(pauser.isPlaying, {
				pauser.stop;
			});
			pauser = Routine({
				synth.set(\doneAction, 0, \wakeUp, 1);
				synth.run(true);
				(server.latency * 2).wait;
				synth.set(\doneAction, doneAction);
			});
			pauser.play;
		});
	}

	playMembrane {
		if(synth.isNil or: { synth.isPlaying.not }, {
			this.initSynth;
		});
		this.awaken;
	}
}

MonoMembrane : GenOrgMembrane {
	*setMembrane { | templater | templater.monoMembrane }
}

StereoMembrane : GenOrgMembrane {
	*setMembrane { | templater | templater.stereoMembrane }
}

QuadMembrane : GenOrgMembrane {
	*setMembrane { | templater | templater.quadMembrane }
}

FOAMembrane : GenOrgMembrane {
	*setMembrane { | templater | templater.foaMembrane }
}

HOAMembrane : GenOrgMembrane {
	*setMembrane { | templater | templater.hoaMembrane }
}
