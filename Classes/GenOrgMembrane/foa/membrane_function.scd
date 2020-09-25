var encoder = FoaEncoderMatrix.newOmni; 
var decoder = FoaDecoderMatrix.newPanto(4, 'flat', 'dual');
{ | modules, in, distance, azimuth, elevation | 
	SynthDef.wrap({ 
		var sig = LPF.ar(in, 18000.0 / distance) 
		* distance.neg.dbamp;
		sig = FoaEncode.ar(sig, encoder); 
		sig = FoaPush.ar(
			sig, 
			pi/2,
			azimuth, 
			elevation
		);
		FoaDecode.ar(sig, decoder);
	});
};
