SpatialNucleus_Quad : SpatialNucleus{
	classvar <orientation = 0.5;
	classvar quadInstances;

	*new{
		var return = super.new(this.prFormatClassSymbol(this));
		//adds a copy to manage all quadInstances of active particles
		quadInstances = quadInstances ? List.new;
		quadInstances.add(return);

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

		classSymbol = this.prFormatClassSymbol(this);
	}

	free{
		super.free;
		quadInstances.remove(this);
	}

	*freeAll{
		quadInstances.do{
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

				var w, x, y, z;
				var distance = \distance.kr(1, lag).clip(1.0, 10000.0);
				var denom = distance.squared;
				var ffreq = 18000.0 / denom;

				var filteredSig = LPF.ar(input, ffreq) * (denom * -1).dbamp;

				#w, x, y, z = PanB.ar(filteredSig, \azimuth.kr(0, lag), \elevation.kr(0, lag));

				DecodeB2.ar(4, w, x, y, orientation);

			};
		};

		synthdef = this.pr_DefineSynthDefShell(wrapperFunction);

		this.registerSynthDef(synthdef, false, symbol);
	}

	*orientation_{
		|newOrientation|
		orientation = newOrientation;

		try{
			this.class.synthDefDictionary !? {
				this.class.synthDefDictionary.removeAt(classSymbol);
			};
		}
	}

	*instances{

		^quadInstances;

	}

}
