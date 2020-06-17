SynthDef.new(\Mutator, { 
	var timescale = \timescale.kr(1); 
	var buf = \buf.kr(0); 
	var buf1 = \buf1.kr(0); 

	/*var sig = 

	Out.ar(0, sig);*/
	//Replace the below with filled out above...
	Out.ar(0, Silent.ar);
}, 
metadata: (
	buf0: 0, 
	buf1: 1, 
	timescale: 1, 
	/*.....*/
));
