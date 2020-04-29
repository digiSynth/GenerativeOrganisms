GenOrg_Factory{
	classvar server;
	classvar <>deleteFiles = false;
	classvar <spatializerClass;
	var <organisms, <buffers, <behaviors;
	var organismCount = 0;
	//this class should be responsible for maintaing the list of spatializers.

	*new{|bufferArray, behaviorArray|
		server = server ? Server.default;

		if(server.hasBooted==false){
			Error("Server has not booted!").throw;
		};

		this.pr_CheckCollectionType(bufferArray, Buffer);

		spatializerClass = spatializerClass ? SpatialCellFOA;

		^super.new.pr_SetupGenerativeOrganismFactory
		(bufferArray, behaviorArray);
	}

	buffers_{ |bufferArray|
		this.class.pr_CheckCollectionType(bufferArray, Buffer);
		buffers = bufferArray;
	}

	behaviors_{|behaviorArray|
		this.class.pr_CheckCollectionType(behaviorArray, GenOrg_Behavior);
		behaviors = behaviorArray;
	}

	*pr_CheckCollectionType{|input, class|
		var shouldErr = false;

		if(input.isCollection.not){

			shouldErr = true;

		}/*ELSE*/{

			block{ |break|

				input.do{|item|

					if(item.class!=class){

						shouldErr = true;
						break.value(999);

					}
				}
			}
		};

		if(shouldErr){

			Error(
				format("Input collection must be a collection of % objects.",
					class.asString)
			).throw;

		};

	}

	pr_SetupGenerativeOrganismFactory{|bufferArray, behaviorArray|

		organisms = Dictionary.new;
		buffers = bufferArray;
		behaviors = behaviorArray;

	}

	pr_MakeOrganism{|bufferIndex, behaviorIndex|

		var spatializer = spatializerClass.new;
		var organism, buffer, behavior;

		bufferIndex = bufferIndex !? {bufferIndex.asInteger} ? {(buffers.size - 1).rand;};

		if(behaviorIndex.isNil.not and: {behaviors.isNil.not}){

			if(behaviorIndex >= behaviors.size){

				behavior = nil;

			}/*ELSE*/{

				behavior = behaviors.removeAt(behaviorIndex.asInteger);

			};
		}/*ELSE*/{

			behavior = nil;

		};


		if(bufferIndex >= buffers.size){

			bufferIndex = (buffers.size - 1).rand;

		};

		buffer = buffers[bufferIndex];

		organismCount = organismCount + 1;

		organism = GenerativeOrganism.new(buffer,
			behavior, spatializer);

		^`organism;
	}

	spawn{|inputSymbol, bufferIndex, behaviorIndex|

		organisms[inputSymbol.asSymbol] ?? {

			organisms.add(inputSymbol.asSymbol ->

				this.pr_MakeOrganism(bufferIndex, behaviorIndex));

		};

	}

	kill{|inputSymbol|

		if(inputSymbol.class!=Symbol){

			try{inputSymbol = inputSymbol.asSymbol};

		};

		organisms[inputSymbol].value !? {

			var organism = organisms.removeAt(inputSymbol).value;

			organism.free(deleteFiles);

		};

	}

	position{|inputSymbol, azimuth = 0, elevation = 0, distance = 1.0|
		var organism;

		if(inputSymbol.class!=Symbol){

			try{inputSymbol = inputSymbol.asSymbol};

		};

		organism = organisms[inputSymbol].value;

		organism !? {

			organism
			.position(
				azimuth: azimuth,
				elevation: elevation,
				distance: distance
			);

		};

	}

	mateOrganisms{
		|partner0Index, partner1Index, newIndex|

		var partner0, partner1;
		var child;

		partner0Index = partner0Index.asSymbol;
		partner1Index = partner1Index.asSymbol;

		partner0 = organisms[partner0Index].value;
		partner1 = organisms[partner1Index].value;

		if(partner0.isNil.not and: {partner1.isNil.not}){

			if(partner0.isGenerativeOrganism and: {
				partner1.isGenerativeOrganism
			}){

				newIndex = newIndex ? organismCount;
				child = partner0.mate(partner1);
				this.addOrganism(newIndex, child);

				organismCount = organismCount + 1;

			};

		};

	}

	eatOrganisms{
		|predatorIndex, preyIndex|

		var predator, prey;

		predatorIndex = predatorIndex.asSymbol;
		preyIndex = preyIndex.asSymbol;

		predator = organisms[predatorIndex].value;
		prey = organisms[preyIndex].value;

		if(predator.isNil.not and: {prey.isNil.not}){

			if(predator.isGenerativeOrganism and:
				{prey.isGenerativeOrganism})

			{
				predator.eat(prey);
				this.kill(preyIndex);
			};

		};
	}

	//[0,       1,         2,    3,          4,      5,     6     ]
	//[ mating, searching, pain, contacting, eating, spawn, death ]
	playGenerativeOrganismFromFactory{|inputSymbol, typeIndex, db = 3|
		var types = GenOrg_Behavior.types, type, organism;

		typeIndex = typeIndex.asInteger;

		if(typeIndex >= types.size){
			typeIndex = (types.size - 1).rand;
		};

		type = types[typeIndex];

		if(inputSymbol.class!=Symbol){
			try{inputSymbol = inputSymbol.asSymbol};
		};

		organism = organisms[inputSymbol].value ;
		organism !? {

			//Make the noise
			organism.playGenerativeOrganism(type, db);

		};

	}

	sound{
		|index, type|

		this.playGenerativeOrganismFromFactory
		(index.asSymbol, type, 0);

	}

	addOrganism{|inputSymbol, organism|

		if(organism.class!=Ref){
			organism = `organism;
		};

		organisms.add(inputSymbol.asSymbol -> organism);
	}

	free{

		if(organisms.isNil.not){

			organisms.copy.keys.do{|key|

				organisms.removeAt(key).value.free;

			};

			GenerativeOrganism.freeAll;
			// GenOrg_Behavior.freeAll;

		};

	}

	*spatializerClass_{
		|newClass|

		if(newClass.isKindOf(SpatialCell).not){
			Error("Can only supply a class of type SpatialCell to this field").throw;
		};

		spatializerClass = newClass;
	}

}