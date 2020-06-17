{ | spatializerUgens | 

	SynthDef(\synth, { 
		var time = \timer.kr(8); 
		var env = EnvGen.kr(
			Env.asr(0.0, 1, \release.kr(1.0)), 
			\gate.kr(1), 
			doneAction: Done.freeSelf
		); 
		var lag = \lag.kr(0.1); 
		var in = In.ar(\in.kr(0), 1) * \ampDB.kr(0).dbamp; 
		var sig = spatializerUgens.value(
			in, 
			\distance.kr(1, lag).clip(1.0, 120.0).squared,
			\azimuth.kr(0, lag).wrap(pi.neg, pi), 
			\elevation.kr(1, lag).wrap(pi.neg, pi), 
			lag
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
