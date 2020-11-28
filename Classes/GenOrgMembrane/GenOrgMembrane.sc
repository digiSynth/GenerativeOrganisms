GenOrgMembrane : GenOrgHybrid {
	var freeList;
	var <input, <output, <synth;
	var <lag, <azimuth = 0, <elevation = 0, <distance = 1;
	var pauser;

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

	initGenOrgHybrid {
		output = 0;
		server.bind({
			input = Bus.audio(server, 1);
			this.initSynth;
		});
		this.initFree;
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

	input_{ | newBus |
		input = newBus;
		if(synth.isPlaying, { synth.set(\in, input) });
	}

	output_{ | newBus |
		output = newBus;
		if(synth.isPlaying, { synth.set(\out, output) });
	}

	initSynth {
		lag = lag ?? { server.latency  * 0.1 };
		synth = Synth.newPaused(modules.synthDef.name, [
			\in, input,
			\out, output,
			\lag, lag,
			\timer, server.latency,
			\doneAction, 0
		]).register;
		synth.onFree({ this.freeList });
	}

	freeList { freeList.do(_.value) }

	initFree {
		var inputCopy = input.copy;
		var outputCopy = output.copy;
		var freeBus = { | bus |
			if(bus.isKindOf(Bus) and: { bus.index.notNil }, {
				bus.free;
			});
		};
		freeList ?? {
			freeList = List.new;
		} !? { freeList.clear };
		freeList.add({
			freeBus.value(inputCopy);
			freeBus.value(outputCopy);
		});
	}

	onFree { | function |
		function !? { freeList.add(function) };
	}

	isRunning { synth !? { ^synth.isRunning } ?? { ^false } }

	isPlaying { ^synth.isPlaying }

	free {
		if(this.isPlaying, {
			if(this.isRunning, {
				if(pauser.isPlaying, { pauser.stop });
				synth.set(\doneAction, 2, \gate, 0);
			}, { synth.free });
		}, { this.freeList });
	}

	setArgument { | key, value |
		if(this.isPlaying, {
			synth.set(key, value);
		});
	}

	azimuth_{ | newAzimuth(pi) |
		azimuth = newAzimuth.wrap(pi.neg, pi);
		this.setArgument(\azimuth, azimuth);
	}

	elevation_{ | newElevation(0) |
		elevation = newElevation.wrap(pi.neg, pi);
		this.setArgument(\elevation, elevation);
	}

	distance_{ | newDistance(2) |
		distance = newDistance.clip(1.0, 100.0);
		this.setArgument(\distance, distance);
	}

	lag_{ |newLag(server.latency)|
		lag = newLag;
		this.setArgument(\lag, lag);
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
		this.awaken(1);
	}

	moduleSet_{ | newSet, from |
		this.free;
		super.moduleSet_(newSet, from);
	}

}