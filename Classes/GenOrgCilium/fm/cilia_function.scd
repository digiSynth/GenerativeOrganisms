{ | modules |
	var freq = ExpRand(1/40, 1/5);
	var fm_ratio = ExpRand(0.1, 4.0);
	var op = SinOsc.ar(freq * fm_fratio);
	SinOsc.kr(freq * op + freq);
}
