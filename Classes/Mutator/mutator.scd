SynthDef(\Mutator, {
		var timescale = \timescale.kr(1);
		var buf0 = \buf0.kr(0);
		var buf1 = \buf1.kr(1);
		var sig0 = PlayBuf.ar(1, buf0,
			BufRateScale.kr(buf0) * \rate0.kr(1))
		* makeEnv.value(BufDur.kr(buf0) * 0.98, 0);

		var sig1 = PlayBuf.ar(1, buf1,
			BufRateScale.kr(buf1) * \rate1.kr(1))
		* makeEnv.value(BufDur.kr(buf1) * 0.98, 0);

		var env = makeEnv.value(timescale * 0.999, 2);
		var chain0 = FFT(LocalBuf(2.pow(10), 1), sig0);
		var chain1 = FFT(LocalBuf(2.pow(10), 1), sig1);
		var morph = PV_Morph(chain0, chain1,
			LFNoise2.kr(timescale * \morphCycles.kr(8)).unipolar(1)
		);
		var out = IFFT(morph) * env * \ampDB.kr(0).dbamp;
		out = Normalizer.ar(out, 1.0);
		Out.ar(\out.kr(0), out);
	},
	metadata: (
		rate0: ControlSpec(0.05, 3.0, 'exp'),
		rate1: ControlSpec(0.05, 3.0, 'exp'),
		timescale: ControlSpec(1, 1),
		morphCycles: ControlSpec(2.0, 32.0, 'exp'),
		out: ControlSpec(0, 0),
		ampDB: ControlSpec(-3, -3)
	)
);
