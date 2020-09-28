{ | modules, buffer0, buffer1, timescale |
	var sig0 = PlayBuf.ar(
		1,
		buffer0,
		BufRateScale.kr(buffer0) * \rate0.kr(
			1,
			spec: ControlSpec(0.1, 4.0, \exp)
		),
		startPos: \startPos0.ir(
			0.0,
			spec: ControlSpec(0.0, 1.0)
		) * BufFrames.kr(buffer0),
		trigger: Impulse.kr(0),
		loop: 1
	);
	var sig1 = PlayBuf.ar(
		1,
		buffer1,
		BufRateScale.kr(buffer1) * \rate1.kr(
			1,
			spec: ControlSpec(0.1, 4.0, \exp)
		),
		startPos: \startPos1.ir(
			0.0,
			spec: ControlSpec(0.0, 1.0)
		) * BufFrames.kr(buffer1),
		trigger: Impulse.kr(0),
		loop: 1
	);
	(sig0 * sig1 * 2).tanh;
};