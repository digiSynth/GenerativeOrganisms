{ | modules, buffer, timescale | 
	PlayBuf.ar(
		1, 
		buffer, 
		BufRateScale.kr(1) * \rate.kr(
			0.5, 
			spec: ControlSpec(0.1, 4.0, \exp)
		), 
		trigger: Impulse.kr(0), 
		startPos: BufFrames.kr(buffer) * Rand(0.0, 1.0), 
		loop: 1
	);
}
