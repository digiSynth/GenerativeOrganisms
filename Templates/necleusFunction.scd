{ | in, lag, orientation(0.5) | 

	SynthDef.wrap({ 
		var distance = \distance.kr(1, lag).squared; 
		var azimuth = \azimuth.kr(1, lag); 
		var elevation = \elevation.kr(1, lag);
		var w, x, y, z; 
		var sig = LPF.ar(in, 18000.0 / distance) * distance.neg.dbamp;
		w, x, y, z = PanB.ar(sig, azimuth, elevation);
		sig = Mix.new(DecodeB2(2, w, x, y, orientation)) / 2; 
	});

}
