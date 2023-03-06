// Template for body of player SynthDef
{ | modules, buffer |
	// Your code here...
	PlayBuf.ar(
		buffer.numChannels,
		buffer,
		BufRateScale.kr(buffer),
		1,
		0,
		0,
		Done.freeSelf
	);
}