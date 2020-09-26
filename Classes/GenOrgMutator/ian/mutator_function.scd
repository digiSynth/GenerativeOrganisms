{ | modules, buffer0, buffer1, timescale |
	var sig0 = PlayBuf.ar(
		1,
		buffer0,
		BufRateScale.kr(buffer0) * \rate0.kr(
			1,
			spec: ControlSpec(0.1, 2.0, \exp)
		);
	);

	var sig1 = PlayBuf.ar(
		1,
		buffer1,
		BufRateScale.kr(buffer1) * \rate1.kr(
			1,
			spec: ControlSpec(0.1, 2.0, \exp)
		);
	);

	var fftsize = 2.pow(10);
	var chain0 = FFT(LocalBuf(fftsize), sig0);
	var chain1 = FFT(LocalBuf(fftsize), sig1);
	var randfreq = \randfreq.kr(
		1.0,
		spec: ControlSpec(0.1, 2.0, \exp)
	);
	var morph = LFNoise2.kr(
		LFNoise1.kr(randfreq)
		.exprange(randfreq * 2, randfreq * 2 * \randHi.kr(
			0.5,
			spec: ControlSpec(1.01, 4.0, \exp)
		))
	).unipolar;
	IFFT(PV_Morph(chain0, chain1, morph));
};
