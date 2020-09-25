GenOrgMembrane : CodexHybrid {
	var freeFunctions, isFreed = false;
	var <group, <inputBus, <outputBus, <synth;
	var <lag, <azimuth, <elevation, <distance;
	var pauser, cilium;

	*contribute { | versions |
		var path = Main.packages.asDict.at(\GenOrg)
		+/+"Classes/GenOrgMembrane";

		versions.add(
			[\mono, path+/+"mono"]
		);

		versions.add(
			[\stereo, path+/+"stereo"]
		);

		versions.add(
			[\quad, path+/+"quad"]
		);

		versions.add(
			[\foa, path+/+"foa"]
		);

		versions.add(
			[\hoa, path+/+"hoa"]
		);
	}

	*makeTemplates { | templater |
		templater.membrane_function;
	}

	initHybrid {
		this.addSynthDef;
		freeFunctions = List.new;
		freeFunctions.add({ this.freeResources });
	}

	addSynthDef {
		var cache = this.class.cache.at(moduleSet);
		if(cache.at(\synthDef).isNil, {
			var synthDef = this.buildSynthDef(modules[\membrane_function]);
			cache.add(\synthDef -> synthDef);
			modules.add(\synthDef -> synthDef);
			this.class.processSynthDefs(moduleSet);
		});
	}

	*addModules { | key |
		this.cache.add(key -> this.loadScripts(key));
	}

	buildSynthDef {
		^SynthDef(\synth, {
			var timer = \timer.kr(8);
			var env = EnvGen.kr(
				Env.asr(0.0, 1, \release.kr(1.0)),
				\gate.kr(1),
				doneAction: Done.freeSelf
			);
			var lag = \lag.kr(0.1);
			var in = In.ar(\in.kr(0), 1) * \ampDB.kr(0).dbamp;
			var sig = modules.membrane_function(
				in,
				\distance.kr(1, lag).clip(1.0, 64).squared,
				\azimuth.kr(0, lag).wrap(pi.neg, pi),
				\elevation.kr(1, lag).wrap(pi.neg, pi),
			);
			var wakeSignal = EnvGen.kr(
				Env.perc(0.0, timer / 2),
				gate: \wakup.tr(1)
			) * PinkNoise.ar;
			DetectSilence.ar(
				in + wakeSignal,
				time: timer / 2,
				doneAction: \doneAction.kr(1)
			);
			Out.ar(\out.kr(0), sig * env);
		});
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

	initGroup {
		group ?? { group = Group.new };
	}

	initSynth {
		this.initResources;
		lag = lag ?? { server.latency  * 0.1 };
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
		function !? { freeFunctions.add(function) };
	}

	isRunning { synth !? { ^synth.isRunning } ?? { ^false } }

	isPlaying { ^synth.isPlaying }

	freeResources {
		group !? { group.free; group = nil };
		this.freeBus(inputBus);
		this.freeBus(outputBus);
		inputBus = outputBus = nil;
	}

	freeBus { | bus |
		if(bus.isKindOf(Bus) and: { bus.index.notNil }, {
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
		if(this.isPlaying, {
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
