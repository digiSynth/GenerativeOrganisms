GenOrgCreature : GenOrgCell {
	var <father, <mother, <lifespan;
	var <age = 0, isKilled = false, <sex;
	var <species, <fitness;
	var <turnsSinceEaten = 0, <turnsSinceMated = 0;
	var <>currentFitness = 1;

	*newFrom { | cell |
		^super.new(
			cell.bufferRef,
			cell.nucleus,
			cell.membrane,
			cell.gene,
			cell.server
		);
	}

	father_{ | newFather |
		if(father.isNil and: { newFather.isKindOf(GenOrgCreature)}){
			father = newFather;
		}{ "This creature already has a father.".warn };
	}

	mother_{ | newMother |
		if(mother.isNil and: { newMother.isKindOf(GenOrgCreature)}){
			mother = newMother;
		}{ "This creature already has a mother.".warn };
	}

	lifespan_{ | newSpan |
		if(lifespan.isNil){
			lifespan = newSpan.asInteger;
		}{ "This creature already has a lifespan.".warn };
	}

	species_{ | newSpecies |
		if(species.isNil){
			species = newSpecies;
		}{ "This creature already has a species".warn };
	}

	initCreature {
		sex = [\f, \m].choose;
		if(father.notNil and: { mother.notNil } and: { lifespan.isNil }){
			this.lifespan = (father.lifespan + mother.lifespan * 0.5) * rrand(0.5, 1.5);
			this.fitness = mother.fitness + father.fitness / 2;
		} {
			species !? {
				this.lifespanRange(
					species.lifespanMin,
					species.lifespanMax
				);

				this.fitnessRange(
					species.fitnessMin,
					species.fitnessMax
				);
			} ?? {
				this.lifespanRange(2, 16);
				this.fitnessRange(0.1, 1.0);
			};
		};
		currentFitness = fitness;
	}

	fitnessRange { | min, max |
		case
		{ min == 0 or: { max == 0} }{
			this.fitness = exprand(min + 1, max + 1) - 1;
		}
		{ min < 0 or: { max < 0 } }{
			this.fitness = exprand(min.abs, max.abs);
		}
		{ this.fitness = exprand(min, max) };
	}

	lifespanRange { | min, max |
		case
		{ min == 0 or: { max == 0} }{
			this.lifespan = exprand(min + 1, max + 1) - 1;
		}
		{ min < 0 or: { max < 0 } }{
			this.lifespan = exprand(min.abs, max.abs);
		}
		{ this.lifespan = exprand(min, max) };
	}

	isMature {
		species ?? { ^true };
		^(age > (lifespan * species.maturityRatio) and: { (turnsSinceMated >= species.matingInterval) });
	}

	/*	isCompatibleWith{ | targetSpecies |
	species !? {
	^species.mates.find([targetSpecies]).notNil;
	};
	^true;
	}*/

	canMateWith { | creature |
		var bool = creature.buffer.notNil and: { this.buffer.notNil };
		bool = bool and: { creature.isDead.not };
		bool = bool and: { this.isRelativeOf(creature).not };
		bool = bool and: { creature.sex != sex };
		bool = bool and: { creature.isMature };
		if((1/3).coin, { this.switchSex });
		^bool;
	}

	mateWith { | creature |
		^super.mateWith(creature)
		.asCreature
		.mother_(this)
		.father_(creature)
		.species_(species)
		.initCreature;
	}

	canEat { | creature |
		if(creature.isKindOf(GenOrgCell)){
			var bool = creature.buffer.notNil and: { this.buffer.notNil };
			creature.species !? {
				bool = bool and: { creature.species.isPreyOf(this) };
			} ?? { bool = bool and: { true } };
			bool = bool and: { creature.isDead.not };
			^bool;
		};
		^false;
	}

	eat { | creature |
		this.mutateWith(creature);
		creature.kill;
		turnsSinceEaten = 0;
	}

	/*	canEat { | targetSpecies |
	species !? {
	^species.prey.find([targetSpecies]).notNil;
	};
	^true;
	}*/

	isRelativeOf { | creature |
		var c0, c1;
		species ?? { ^false };
		if(species.matingDistance.notNil and: { species.matingDistance > 0}){
			2.do { | p |
				c1 = this;
				species.matingDistance.do { | i |
					if(i==0 and: { p!=0 }){
						c1 = c1.father;
					};
					2.do { | x |
						c0 = creature;
						species.matingDistance.do { | y |
							if(c0.isNil or: { c1.isNil }){ ^false };
							if(c0.isParentOf(c1) or: { c1.isParentOf(c0) }){ ^true };
							if(x==0){ c0 = c0.mother }{ c0 = c1.father };
						};
					};
					if(p==0){ c1 = c1.mother }{ c1 = c1.father };
				}
			}
		};
		^false;
	}

	isParentOf { | creature |
		^(this==creature.father or: { this==creature.mother });
	}

	update { | turns(1) |
		turnsSinceMated = turnsSinceMated + turns;
		turnsSinceEaten = turnsSinceEaten + turns;
		age = age + turns;
	}

	isDead {
		lifespan !? {
			^(age >= lifespan or: {
				turnsSinceEaten >= (species.hungerInterval * species.starvationPeriod).ceil;
			});
		} ?? { ^false }
	}

	kill {
		lifespan ?? { lifespan = 1 };
		age = lifespan * 2
	}

	switchSex {
		if(sex==\f, { sex = \m }, { sex = \f });
	}

	play { | timescale(1), out(0), target, addAction(\addToHead) |
		this.playCell(timescale, out, target, addAction);
	}

	isHungry {
		if(this.isDead){ ^false };
		^(turnsSinceEaten >= species.hungerInterval);
	}

	fitness_{ | newFitness |
		fitness ?? { fitness = newFitness } !? {
			if(newFitness > fitness){
				fitness = newFitness;
			};
		};
	}

	isPreyOf { | creature |
		species !? { ^species.isPreyOf(creature) } ?? { ^true };
	}

	isMateOf { | creature |
		species !? { ^species.isMateOf(creature) } ?? { ^true };
	}
}

GenOrgSpecies {
	var <prey, <mates, <>matingDistance = 4, <>maturityRatio = 0.25;
	var <>nucleusSet, <>membraneSet, <>geneSet, <>ciliumSet;
	var <startingPop = 16, <>maxPop = 128;
	var <buffers, <server;
	var <>hungerInterval = 5;
	var <>starvationPeriod = 4;
	var <>matingInterval = 2;
	var <>lifespanMin = 2;
	var <>lifespanMax = 16;
	var <>fitnessMin = 0.5;
	var <>fitnessMax = 1.0;
	var <>timescaleRatio = 1.0;
	var <>folder, <>fileTemplate;

	*new { | prey, mates, nucleusSet(\tgrains), membraneSet(\stereo), geneSet(\morph), ciliumSet(\lfnoise2), server(Server.default) |
		^super.new
		.prey_(prey)
		.mates_(mates)
		.nucleusSet_(nucleusSet)
		.membraneSet_(membraneSet)
		.geneSet_(geneSet)
		.ciliumSet_(ciliumSet)
		.server_(server)
		.folder_("~/mutations".standardizePath)
		.fileTemplate_("mutation.wav");
	}

	prProcessInput { | input |
		case
		//If it is nil, return a single-sized array of the instance
		{ input.isNil }{ ^[ this ] }
		//If it is not a collection
		{ input.isCollection.not }{

			if(input.isKindOf(GenOrgSpecies).not){
				this.prThrowError;
			};
			//But it is a GenOrgSpecies, return it in a single-sized array
			^[ input ]
		}
		//Otherwise, filter out the non-species and return.
		{
			input = input.select({ | item |
				item.isKindOf(GenOrgSpecies);
			});

			if(input.isNil or: { input.isEmpty }){
				this.prThrowError;
			};

			^input;
		};
	}

	prThrowError {
		Error("Can only set with instance of GenOrgSpecies").throw;
	}

	prey_{ | newPrey |
		prey = this.prProcessInput(newPrey);
	}

	mates_{ | newMates |
		mates = this.prProcessInput(newMates);
	}

	buffers_{ | newBuffers |
		if(newBuffers.isCollection, {
			buffers = newBuffers
		});
	}

	server_{ | newServer |
		if(newServer.isKindOf(Server)){
			server = newServer;
		};
	}

	startingPop_{ | newMin |
		if(newMin > maxPop, { maxPop = newMin });
		startingPop = newMin;
	}

	isPreyOf { | species |
		if(species.isKindOf(GenOrgCreature)){
			species.species ?? { ^true } !? {
				species = species.species;
			}
		};
		^species.prey.find([this]).notNil;
	}

	isMateOf { | species |
		if(species.isKindOf(GenOrgCreature)){
			species.species ?? { ^true } !? {
				species = species.species;
			}
		};
		^species.mates.find([this]).notNil;
	}

}
