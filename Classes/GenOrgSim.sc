GenOrgSim {
	var <speciesArray, <populations, <routine;
	var <>out = 0, <>group, <>addAction;
	var <>delta = 0.1;

	*new { | speciesArray |
		^super.newCopyArgs(speciesArray)
		.initSim;
	}

	initSim {
		populations = Dictionary.new;
		speciesArray.do { | species |
			var buffers = species.buffers.select({ | b |
				b.isKindOf(Buffer);
			});
			var list = Array.fill(
				species.startingPop,
				{ | i |
					var creature = GenOrgCreature(
						buffers[i % buffers.size],
						GenOrgNucleus(species.nucleusSet),
						GenOrgMembrane(species.membraneSet),
						GenOrgGene(species.geneSet),
						species.server
					);
					creature.species = species;
					creature.initCreature;
					creature;
				}
			).asList;
			populations.add(species -> list);
		};
		this.makeRoutine;
	}

	start { this.play }

	play {
		routine ?? { this.makeRoutine };
		if(routine.isPlaying.not, { routine.play });
	}

	pause {
		routine ?? { this.makeRoutine } !? { routine.pause }
	}

	resume {
		routine ?? { this.play } !? { routine.resume };
	}

	takeHunting { | creature |
		//Try to eat
		if(creature.isHungry, {
			//Go hunting
			var where = populations.keys.asArray.scramble;
			if(creature.canEat(where)){
				//Find some prey
				var target = populations[where].choose;
				//There's a chance you might eat it.
				if((creature.currentFitness / (target.currentFitness + creature.currentFitness)).coin){
					var difference = abs(creature.currentFitness - target.currentFitness);
					creature.eat(target);
					creature.currentFitness = (creature.currentFitness+difference*rrand(0.5, 1.5));
					//If in your conquests you become super huge, rewrite your DNA.
					if(creature.currentFitness > 1){
						creature.fitness = creature.currentFitness;
					};
					^this;
				}
				//Otherwise...
				{
					//If it is stronger than you...
					if(target.currentFitness >= creature.currentFitness){
						//This all might not turn out so well.
						if((target.currentFitness - creature.currentFitness).coin){
							var subtraction = creature.currentFitness - target.currentFitness.rand;
							case
							//You could be killed...
							{ subtraction <= 0}{ creature.kill }
							//Hurt...
							{ subtraction.coin }{ creature.currentFitness = subtraction }
							//Or you might survive to live another day...
							{};

						};
					};
				}
			};

			if(creature.isDead.not){
				//In any case, you'll be hungrier than you were before so you'll also be weaker.
				creature.currentFitness = creature.currentFitness
				- (creature.currentFitness * creature.species.hungerPeriod.reciprocal * rrand(0.5, 1.5));

				//Which means you might still die.
				if(creature.currentFitness <= 0){ creature.kill };
			};

		});
	}

	helpWithMating { | creature |
		var t_species = populations[creature.species].size;
		//If nature calls
		if(creature.isMature and: { t_species < creature.species.maxPop }, {
			//Look for mates
			var where = populations.keys.asArray.scramble;
			if(creature.isCompatibleWith(where)){
				//Find a specific mate
				var target = populations[where].choose;
				if(target.isMature, {
					//Then try it out
					var child = creature.mateWith(target);
					child !? {
						//If a child came about, add it to the population.
						populations[creature.species].add(child);
					}
				});
			};
		});
	}

	makeRoutine {
		routine = Routine({
			loop {
				var keys = populations.keys.asArray.scramble;
				keys.do{ | species |
					var population = populations[species];
					population.do { | creature |
						creature.update;
						if(creature.isDead, {
							population.remove(creature).free;
						}, {
							//Try to eat
							this.takeHunting(creature);
							//Try to mate
							this.helpWithMating(creature);

							//In any case, let's hear it.
							if(creature.isDead.not, {
								creature.play(
									creature.currentFitness,
									out,
									group,
									addAction
								);
							});
						});
					};
					delta.wait;
				};
			}
		})
	}

}