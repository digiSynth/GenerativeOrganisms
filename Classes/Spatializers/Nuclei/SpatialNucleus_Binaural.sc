SpatialNucleus_Binaural : SpatialNucleus{
	classvar <order = 4, <headphoneCorrectionIndex = 22;
	classvar <classSymbol, binauralInstances;
	classvar headphonesLoaded = false;
	classvar binauralIRsLoaded = false;

	*new{
		var return = super.new(this.prFormatClassSymbol(this));
		//adds a copy to manage all binauralInstances of active particles
		binauralInstances = binauralInstances ? List.new;
		binauralInstances.add(return);

		^return;
	}

	*initNew{
		super.initNew(this.prFormatClassSymbol(this));
	}

	*pr_InitializeSpatialCell{
		//set up the class symbol:
		//this will be used to format synth names as well as to
		//manage loading synthdefs onto the server by the super
		//class so that every calling of the class does
		//not also accompany a reloading of redundant synthdefs

		if(binauralIRsLoaded==false){
			HOABinaural.loadbinauralIRs(server);
			binauralIRsLoaded = true;
		};

		if(headphonesLoaded==false){
			HOABinaural.loadHeadphoneCorrections(server);
			headphonesLoaded = true;
		};

		classSymbol = this.prFormatClassSymbol(this);
	}

	free{
		super.free;
		binauralInstances.remove(this);
	}

	*freeAll{
		binauralInstances.do{
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

				HOABinaural.ar(order, sig,
					headphoneCorrection: headphoneCorrectionIndex ? 22);

			};
		};

		synthdef = this.pr_DefineSynthDefShell(wrapperFunction);

		this.registerSynthDef(synthdef, false, symbol);
	}

	*order_ {
		|newOrder|
		order = newOrder;

		try{
			this.class.synthDefDictionary !? {
				this.class.synthDefDictionary.removeAt(classSymbol);
			};
		}
	}

	*headphoneCorrectionIndex_{
		|newIndex|
		headphoneCorrectionIndex = newIndex;

		try{
			this.class.synthDefDictionary !? {
				this.class.synthDefDictionary.removeAt(classSymbol);
			};
		}
	}

	*headphoneList{
		var headphones = HOABinaural.headPhones;

		^headphones.collect({|item, index|
			[index, item];
		});
	}

	*instances{

		^binauralInstances;

	}
}