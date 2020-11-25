GenOrgCreature : GenOrgCell {
	var <father, <mother, <lifespan;
	var <age = 0, time, isKilled = false, <sex;
	var <species;

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
		sex = [\f, \m].choose;
		if(father.notNil and: { mother.notNil } and: { lifespan.isNil }){
			this.lifespan = (father.lifespan + mother.lifespan * 0.5) * rrand(0.5, 1.5);
		} { this.lifespan = 2.pow(exprand(5, 8)) };
	}

	isMature {
		species ?? { ^true };
		^(age > (lifespan * species.matingRatio));
	}

	canMateWith { | creature |
		if(creature.isKindOf(GenOrgCreature)){
			var bool = this.isRelativeOf(creature).not;
			bool = bool and: { creature.sex != sex };

			species !? {
				bool = bool and: {
					species.mates.find([creature.species]).notNil;
				};
			};

			if(0.15.coin, { this.switchSex });
			^bool;
		};
		^false;
	}

	mateWith { | creature |
		if(this.canMateWith(creature), {
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
		};
	}

	canEat { | creature |
		species !? {
			^species.prey.find([creature.species]).notNil;
		};
		^true;
	}

	isRelativeOf { | creature |
		var c0, c1;
		species ?? { ^false };
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
			^(age >= lifespan)
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
}

GenOrgSpecies {
	var <prey, <mates, <>matingDistance = 4, <>matingRatio = 0.25;
	var <>nucleusSet, <>membraneSet, <>geneSet;
	var <>minPop = 12, <>maxPop = 256;
	var <buffers, server;

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
}
