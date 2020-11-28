GenOrgSim {
	var <speciesArray, <populations, <routine;
	var <>out = 0, <>group, <>addAction;
	var <>delta = 0.1;
	var <graveyard, <zombies;

	*new { | speciesArray |
		^super.newCopyArgs(speciesArray)
		.initSim;
	}

	initSim {
		populations = Dictionary.new;

		graveyard ?? { graveyard = List.new } !? {
			forkIfNeeded {
				this.emptyGraveyard;
				graveyard.clear;
			};
		};

		zombies ?? { zombies = List.new } !? {
			forkIfNeeded {
				this.emptyGraveyard;
				zombies.clear;
			};
		};

		fork{
			speciesArray.do { | species |
				var server = species.server;
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
						creature.addAzimuth(GenOrgCilium(species.ciliumSet));
						creature.addElevation(GenOrgCilium(species.ciliumSet));
						creature.addDistance(GenOrgCilium(species.ciliumSet));
						creature.species = species;
						creature.folder = species.folder;
						creature.fileTemplate = species.fileTemplate;
						creature.initCreature;
						server.sync;
						creature;
					};
				).asList;
				populations.add(species -> list);
				species.folder.mkdir;
				// if(File.exists(species.folder).not, { species.folder.mkdir });
				server.sync;
			};
		};
		this.makeRoutine;
	}

	play { | clock(AppClock) |
		routine ?? { this.makeRoutine };
		if(routine.isPlaying.not, { routine.play(clock) });
	}

	takeHunting { | creature |
		//Go hunting.
		var where = populations.keys.asArray.scramble.choose;
		var target = populations[where].choose;
		//If you find something edible,
		if(creature.canEat(target)){
			"An EATING HAPPENS".postln;
			//There's a chance you might eat it.
			if((creature.currentFitness / (target.currentFitness + creature.currentFitness)).coin){
				var difference = abs(creature.currentFitness - target.currentFitness);
				creature.eat(target);
				creature.currentFitness = (creature.currentFitness+difference*rrand(0.5, 1.5));
				//If in your eating you become super huge, rewrite your DNA.
				if(creature.currentFitness > 1){
					creature.fitness = creature.currentFitness;
				};
				^this;
			}
			//Otherwise...
			{
				//If the prey is stronger than you...
				if(target.currentFitness >= creature.currentFitness){
					//This all might not turn out so well.
					if((target.currentFitness - creature.currentFitness).coin){
						var subtraction = creature.currentFitness - target.currentFitness.rand;
						case
						//You could be killed...
						{ subtraction <= 0}{ creature.kill }
						//Hurt...
						{ subtraction.coin }{ creature.currentFitness = subtraction }
						//Or you might survive to live another day.
						{};

					};
				};
			}
		};

		if(creature.isDead.not){
			//In any case, you'll be hungrier than you were before so you'll also be weaker.
			creature.currentFitness = creature.currentFitness
			- (creature.currentFitness * creature.species.hungerInterval.reciprocal * rrand(0.5, 1.5));

			//Which means you might still die.
			if(creature.currentFitness <= 0){ creature.kill };
		};

	}

	helpWithMating { | creature |
		var t_species = populations[creature.species].asArray.size;
		//If nature calls,
		if(creature.isMature and: { (t_species < creature.species.maxPop) }, {
			//Look for mates.
			var where = populations.keys.asArray.scramble.choose;
			var target;
			"A MATIGN HAPPENS HERE!".postln;
			if((creature.currentFitness / creature.fitness).coin, {
				var mates = populations[where].select({ | item |
					creature.canMateWith(item);
				});
				target = mates.choose;
			});

			if(target.notNil and: { creature.canMateWith(target) }){
				fork {
					var child;
					//Then try it out.
					child = creature.mateWith(target);
					creature.server.sync;
					child !? {
						//If a child came about, add it to the population.
						populations[creature.species].add(child);
					}
				};
			};
		});
	}

	makeRoutine {
		routine = Routine({ | inval |
			CmdPeriod.doOnce({
				var creatures = populations.asArray.flat.copy;
				creatures.do(_.free);
			});
			loop {
				var creatures = populations.asArray.flat.scramble;
				if(populations.asArray.flat.isEmpty, {
					"!Everything is dead!".postln;
					thisThread.stop;
				});
				creatures.do{ | creature |
					var species = creature.species;
					creature.update;
					if(creature.isDead, {
						var corpse = populations[species].remove(creature);
						"...dead".warn;
						graveyard.add(corpse);
						/*creature.server.bind({
						creature.free;
						});*/
					}, {
						//Try to eat
						if(creature.isHungry, {
							this.takeHunting(creature);
						}, { this.helpWithMating(creature ) });


						if(creature.isDead.not, {
							creature.play(
								creature.currentFitness * species.timescaleRatio,
								out,
								group,
								addAction
							);
						});
						delta.wait;
					});
				};
				delta.wait;
				this.emptyGraveyard;
			}
		}, 2.pow(20));
	}


	killAll {
		var kill = { | dict |
			var array = dict.asArray.flat;
			if(array.isEmpty.not){
				array.do { | creature |
					if(creature.mother.notNil){
						creature.buffer.free
					};
					creature.free;
				};
			};
		};

		kill.value(populations);
		populations.clear;

		kill.value(graveyard);
		graveyard.clear;

		kill.value(zombies);
		zombies.clear;
	}

	*initClass {
		Class.initClassTree(Event);
		Event.addEventType(\genOrgSim, {
			var sim = ~sim.value ?? { "No instance of GenOrgSim specified".warn };
			sim.delta = ~dur.value ? 1;
			// sim.group = ~group.value ? Server.default.defaultGroup;
			// sim.addAction = ~addAction.value ? \addToHead;
			sim.out = ~out.value ? 0;
			sim.routine.next;
		});
	}

	emptyGraveyard {
		if(graveyard.isEmpty.not){
			fork {
				var server = graveyard[0].species.server;
				var copyYard = graveyard.copy;
				graveyard.do(_.free);
				server.sync;
				copyYard.do { | creature |
					var target = graveyard.remove(creature);
					if(target.membrane.synth.isPlaying){
						zombies.add(creature);
					};
				};
				server.sync;
				this.killZombies;
			};
		};
	}

	killZombies {
		if(zombies.isEmpty.not){
			fork {
				var server = zombies[0].species.server;
				var copyZombies = zombies.copy;
				zombies.do { | creature |
					creature.free;
				};
				server.sync;
				copyZombies.do { | creature |
					if(creature.membrane.synth.isPlaying){
						creature.membrane.synth.free;
					};
					zombies.remove(creature);
				};
			};
		};
	}

	free { this.killAll }
}