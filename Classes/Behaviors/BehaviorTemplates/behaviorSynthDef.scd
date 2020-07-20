{ | modules |
	var envs = modules.behaviorEnvsWrappers(modules.behaviorEnvs);
	SynthDef(\synth, { 
		var buf = \buf.kr(0); 
		var timescale = \timescale.kr(1); 
		var bufDur = BufDur.kr(buf); 
		var impulseRate = envs[\impulseRate].value(timescale); 
		var impulse = Impulse.ar(impulseRate);
		var pointer = env[\pos].value(timescale * bufDur) 
		+ TRand.kr(0.005.neg, 0.005, impulse); 
		var sig = TGrains.ar(
			1, 
			impulse, 
			buf,
			rate, 
			pointer.wrap(0, bufDur), 
			envs[\grainDur].value(timescale)
		) * 16;
		sig = BPF.ar(
			sig, 
			envs[\ffreq].value(timescale), 
			envs[\rq].value(timescale)
		); 
		sig = sig * EnvGen.ar(
			Env([0, 1, 1, 0], [0.1, 1, 0.1].normalizeSum, \sine), 
			timeScale: timescale, 
			doneAction: Done.freeSelf
		);
		Out.ar(\out.kr(0), out);
	});
};
