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
	var randfreq = ExpRand(1/10, 2.0); 
	//Continue from here
};
