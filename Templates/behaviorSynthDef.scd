{ | modules |
	var envs = modules.envs;
	SynthDef(\synth, {
		var buf = \buf.kr(0);
		var timescale = \timescale.kr(1);
		var bufDur = BufDur.kr(buf);
		var impulseRate = envs[\impulseRate].value(timescale);
		var impulse = Impulse.ar(impulseRate);
		var pointer = envs[\pos].value(timescale * bufDur)
		+ TRand.kr(0.005.neg, 0.005, impulse);
		var sig = TGrains.ar(
			1,
			impulse,
			buf,
			envs[\rate].value(timescale),
			pointer.wrap(0, bufDur),
			envs[\grainDur].value(timescale)
		) * 16;
		sig = BPF.ar(
			sig,
			envs[\ffreq].value(timescale),
			envs[\rq].value(timescale)
		);
		sig = sig * envs[\amp].value(timescale) * EnvGen.ar(
			Env([0, 1, 1, 0], [0.1, 1, 0.1].normalizeSum, \sine),
			timeScale: timescale,
			doneAction: Done.freeSelf
		);
		Out.ar(\out.kr(0), sig * \ampDB.kr(0).dbamp);
	});
};
