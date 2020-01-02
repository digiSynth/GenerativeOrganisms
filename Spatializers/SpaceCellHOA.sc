SpaceCellHOA : SpaceCell{
	classvar <classSymbol, hoaInstances;
	// classvar encoder, decoder;
	classvar <order = 2, orderChannels;
	classvar <decoderOrder = 1, decoderOrderChannels;


	*new{
		var return = super.new(this.prFormatClassSymbol(this));
		//adds a copy to manage all hoaInstances of active particles
		hoaInstances = hoaInstances ? List.new;
		hoaInstances.add(return);

		if(orderChannels.isNil){
			orderChannels = (order + 1).pow(2);
		};

		if(decoderOrderChannels.isNil){
			decoderOrderChannels = (decoderOrder + 1).pow(2);
		};

		^return;
	}

	*initNew{
		super.initNew(this.prFormatClassSymbol(this));
	}

	*pr_InitializeSpaceCell{
		//set the FOA encoder to use
		// encoder = nil;

		//set the FOA decoder to use
		// decoder = nil;

		//set up the class symbol:
		//this will be used to format synth names as well as to
		//manage loading synthdefs onto the server by the super
		//class so that every calling of the class does
		//not also accompany a reloading of redundant synthdefs
		classSymbol = this.prFormatClassSymbol(this);
	}

	free{
		super.free;
		hoaInstances.remove(this);
	}

	*freeAll{
		hoaInstances.do{
			|item|
			item.free;
		}
	}

	*defineSynthDefs{
		var symbol = this.prFormatClassSymbol(this);
		//define the synth defs in this method
		var synthdef;
		var wrapperFunction = { |input, lag|
			{
				var sig = HOAEncoder.ar(
					order,
					input,
					\azimuth.kr(0, lag),
					\elevation.kr(0, lag),
					plane_spherical: 1,
					radius: \distance.kr(0, lag);
				);

				var decodedSig = HOADecLebedev26.ar(decoderOrder, sig);
				decodedSig;
			};
		};

		synthdef = this.pr_DefineSynthDefShell(wrapperFunction);

		this.registerSynthDef(synthdef, false, symbol);
	}

	*order_ {
		|newOrder|
		order = newOrder;
		orderChannels = (order + 1).pow(2);

		try{
			this.class.synthDefDictionary !? {
				this.class.synthDefDictionary.removeAt(classSymbol);
			};
		}
	}

	*decoderOrder_{
		|newOrder|
		decoderOrder = newOrder;
		decoderOrderChannels = (decoderOrder + 1).pow(2);

		try{
			this.class.synthDefDictionary !? {
				this.class.synthDefDictionary.removeAt(classSymbol);
			};
		}
	}

	*instances{

		^hoaInstances;

	}
}