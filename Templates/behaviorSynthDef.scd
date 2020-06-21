{ | modules |
	var envs = modules.envs;
	SynthDef(\synth, {
		var buf = \buf.kr(0);
		var bufDur = BufDur.kr(buf);
		var timescale = \timescale.kr(1);
		var impulseRate = envs.impulseRate(timescale);
		var impulse = Impulse.ar(impulseRate);
		var pointer = envs.pos(timescale)
		+ TRand.kr(0.005.neg, 0.005, impulse);
		var sig = TGrains.ar(
			1,
			impulse,
			buf,
			envs.rate(timescale),
			pointer.wrap(0, bufDur),
			envs.grainDur(timescale)
		) * 16;
		sig = BPF.ar(
			sig,
			envs.ffreq(timescale),
			envs.rq(timescale)
		);
		sig = Normalizer.ar(sig) * envs.amp(timescale) * EnvGen.ar(
			Env([0, 1, 1, 0], [0.1, 1, 0.1].normalizeSum, \sine),
			timeScale: timescale,
			doneAction: Done.freeSelf
		);
		Out.ar(\out.kr(0), sig * \ampDB.kr(0).dbamp);
	});
};
