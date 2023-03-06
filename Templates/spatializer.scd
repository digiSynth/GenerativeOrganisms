// Template for spatializer of player SynthDef
{ | modules, sig, x, y, z |
	// Your code here...
	Pan2.ar(sig, x.clip(-1.0, 1.0));
};