{ | modules, in, distance, azimuth, elevation | 
	var orientation = 0.5;
	var w, x, y, z; 
	var sig = LPF.ar(in, 18000.0 / distance) * distance.neg.dbamp;
	#w, x, y, z = PanB.ar(sig, azimuth, elevation);
	DecodeB2(1, w, x, y, orientation);
};
