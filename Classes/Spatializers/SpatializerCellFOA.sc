SpatialCellFOA : SpatialCell{
	classvar <classSymbol, foaInstances;
	classvar encoder, decoder;

	*new{
		var return = super.new(this.prFormatClassSymbol(this));
		//adds a copy to manage all foaInstances of active particles
		foaInstances = foaInstances ? List.new;
		foaInstances.add(return);

		^return;
	}

	*initNew{
		super.initNew(this.prFormatClassSymbol(this));
	}

	*pr_InitializeSpatialCell{
		//set the FOA encoder to use
		encoder = FoaEncoderMatrix.newOmni;

		//set the FOA decoder to use
		decoder = FoaDecoderMatrix.newPanto(4, 'flat', 'dual');

		//set up the class symbol:
		//this will be used to format synth names as well as to
		//manage loading synthdefs onto the server by the super
		//class so that every calling of the class does
		//not also accompany the reloading of redundant synthdefs
		classSymbol = this.prFormatClassSymbol(this);
	}

	free{
		super.free;
		foaInstances.remove(this);
	}

	*freeAll{
		foaInstances !? {

			if(foaInstances.size > 0){
				foaInstances.copy.do{
					|item|
					item.free;
				}

			}

		}
	}

	*defineSynthDefs{
		var symbol = this.prFormatClassSymbol(this);
		//define the synth defs in this method
		var synthdef;
		var wrapperFunction = { |input, lag|
			{
				var distance = \distance.kr(1, lag).clip(1.0, 10000.0);
				var denom = distance.squared;
				var ffreq = 18000.0 / denom;


				var filteredSig = LPF.ar(input, ffreq) * (denom * -1).dbamp;

				var sig = FoaEncode.ar(filteredSig, encoder);

				sig = FoaPush.ar(
					sig,
					pi/2,
					\azimuth.kr(0, lag),
					\elevation.kr(0, lag)
				);

				//this proximity class makes terrible clickings. Too bad...
				// sig = FoaProximity.ar(sig, \distance.kr(0.1, lag).clip(1e-4, 20000));

				FoaDecode.ar(sig, decoder);

			};
		};

		synthdef = this.pr_DefineSynthDefShell(wrapperFunction);

		this.registerSynthDef(synthdef, false, symbol);
	}


	*decoder_{|newDecoder|

		if(newDecoder.class!=FoaDecoderMatrix or: {newDecoder.class!=FoaDecoderKernel}){
			Error.throw("newDecoder must either be of type decoder or FoaDecoderKernel").throw;
		};

		//change the decoder
		decoder = newDecoder;
		//remove the synthdefs from the super class's dictionary
		//so that it reloads them with the new decoder when the next object is called
		super.synthDefDictionary.removeAt(classSymbol);
	}


	*encoder_{|newEncoder|

		if(newEncoder.class!=FoaEncoderMatrix or: {newEncoder.class!=FoaEncoderKernel}){
			Error.throw("newEncoder must either be of type FoaEncoderMatrix or FoaEncoderKernel").throw;
		};

		//change the decoder
		encoder = newEncoder;
		//remove the synthdefs from the super class's dictionary
		//so that it reloads them with the new decoder when the next object is called
		super.synthDefDictionary.removeAt(classSymbol);
	}

	*instances{

		^foaInstances;

	}

}
