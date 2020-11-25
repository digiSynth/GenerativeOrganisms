GenOrgCreature : GenOrgCell {
	var <father, <mother, <lifespan;
	var <age = 0, time, isKilled = false, <sex;
	var <species;
	var timeSinceEaten, timeSinceMated;
	var <fitness;
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
			lifespan = newSpan;
		}{ "This creature already has a lifespan.".warn };
	}

	species_{ | newSpecies |
		if(species.isNil){
			species = newSpecies;
		}{ "This creature already has a species".warn };
	}

	initCreature {
		time = Main.elapsedTime;
		timeSinceEaten = time;
		sex = [\f, \m].choose;
		if(father.notNil and: { mother.notNil } and: { lifespan.isNil }){
			this.lifespan = (father.lifespan + mother.lifespan * 0.5) * rrand(0.5, 1.5);
			fitness = mother.fitness + father.fitness / 2;
		} {
			this.lifespan = 2.pow(exprand(5, 8));
			fitness = 1.0.rand;
		};
		currentFitness = fitness;
	}

	isMature {
		species ?? { ^true };
		^(age > (lifespan * species.maturityRatio) and: { timeSinceMated >= species.matingPeriod });
	}

	isCompatibleWith{ | targetSpecies |
		species !? {
			^species.mates.find([targetSpecies]).notNil;
		};
		^true;
	}

	canMateWith { | creature |
		if(creature.isKindOf(GenOrgCreature)){
			var bool = this.isRelativeOf(creature).not;
			bool = bool and: { creature.sex != sex };
			if(0.15.coin, { this.switchSex });
			^bool;
		};
		^false;
	}

	mateWith { | creature |
		if(this.canMateWith(creature), {
			timeSinceMated = Main.elapsedTime;
			^super.mateWith(creature).asCreature
			.mother_(this)
			.father_(creature)
			.species_(species)
			.initCreature;
		});
		^nil;
	}

	eat { | creature |
		if(this.canEat(creature)){
			this.mutateWith(creature);
			creature.kill;
			timeSinceEaten = Main.elapsedTime;
		};
	}

	canEat { | targetSpecies |
		species !? {
			^species.prey.find([targetSpecies]).notNil;
		};
		^true;
	}

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

	update { | deltaTime |
		if(deltaTime.notNil, {
			age = age + deltaTime;
		}, {
			var previousTime = time;
			time = Main.elapsedTime;
			this.update(time - previousTime);
		});
	}

	isDead {
		lifespan !? {
			^(age >= lifespan or: {
				((Main.elapsedTime - timeSinceEaten)
					>= (species.hungerPeriod * species.starvationInterval));
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
		^((Main.elapsedTime - timeSinceEaten) >= species.hungerPeriod);
	}

	fitness_{ | newFitness |
		fitness ?? { fitness = newFitness } !? {
			if(newFitness > fitness){
				fitness = newFitness;
			};
		};
	}
}

GenOrgSpecies {
	var <prey, <mates, <>matingDistance = 4, <>maturityRatio = 0.25;
	var <>nucleusSet, <>membraneSet, <>geneSet;
	var <startingPop = 16, <>maxPop = 128;
	var <buffers, <server;
	var <>hungerPeriod = 5;
	var <>starvationInterval = 4;
	var <>matingPeriod = 2;

	*new { | prey, mates, nucleusSet(\tgrains), membraneSet(\stereo), geneSet(\morph), server(Server.default) |
		^super.new
		.prey_(prey)
		.mates_(mates)
		.nucleusSet_(nucleusSet)
		.membraneSet_(membraneSet)
		.geneSet_(geneSet)
		.server_(server)
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
}
