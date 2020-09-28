{ | modules | 
	var randfreq = ExpRand(1/20, 1/5);
	var randmod = LFNoise1.kr(randfreq)
	.exprange(
		randfreq * 2, 
		randfreq * 2 * ExpRand(1.05, 8.0)
	);
	LFNoise2.kr(randmod)
};
