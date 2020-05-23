SpatialCell{

	var <nucleus, <cilium, <type;

	*new{ |numChannels = 1, cilium|

		^super.new
		.nucleus_(numChannels)
		.cilium_(cilium);

	}

	nucleus_{|targetNucleus|

		if(nucleus.isNil.not){
			if(nucleus.isPlaying){
				nucleus.free;
			};
			nucleus = nil;
		};

		if(targetNucleus.isKindOf(SpatialNucleus)){
			nucleus = targetNucleus;
		}/*ELSE*/{

			if(targetNucleus.isKindOf(Float)){
				targetNucleus = targetNucleus.asInteger;
			};

			if(targetNucleus.asString.toLower=="binaural"){
				targetNucleus = -2;
			};

			if(targetNucleus.asString.toLower=="foa"){
				targetNucleus = -1;
			};

			if(targetNucleus.asString.toLower=="hoa"){
				targetNucleus = -4;
			};

			if(targetNucleus.isKindOf(Integer)){
				switch(targetNucleus,
					-4, {nucleus = SpatialNucleus_HOA.new; type = \hoa},
					-2, {nucleus = SpatialNucleus_Binaural.new; type = \binaural;},
					-1, {nucleus = SpatialNucleus_HOA.ew; type = \foa},
					0, {nucleus = SpatialCell_Mono.new; type = \mono;},
					1,  {nucleus = SpatialCell_Mono.new; type = \mono;},
					2, {nucleus = SpatialCell_Stereo.new; type = \stereo;},
					4,  {nucleus = SpatialCell_Quad.new; type = \quad},
					{

						var toTest = targetNucleus.abs;
						var errorMsg = format("% channel configuration not supported", toTest);
						Error(errorMsg).throw;

					};
				)
			};

		};
	}

	cilium_{|newCilium|
		if(nucleus.isPlaying){
			this.attachCilium(cilium);
		};
	}

	attachCilium{|targetCilium|
		if(targetCilium.isNil.not){
			if(targetCilium.isKindOf(SpatialCilium).not){
				Error("Can only map a SpatialCilium to a SpatialCell").throw;
			};
		}/*ELSE*/{
			targetCilium = SpatialCilium.new;
		};

		targetCilium.attachCiliumTo(this);
		cilium = targetCilium;
	}

	detachCilium{
		if(cilium.isNil.not){
			cilium.free;
		};
	}

	map{|argumentKey, toMap|
		if(nucleus.isPlaying){
			nucleus.synth.map(argumentKey, toMap);
		};
	}

	set{|...args|
		if(nucleus.isPlaying){
			nucleus.synth.set(args);
		};
	}

	group{
		if(nucleus.isPlaying){
			^nucleus.group;
		}/*ELSE*/{
			^nil;
		};
	}

}