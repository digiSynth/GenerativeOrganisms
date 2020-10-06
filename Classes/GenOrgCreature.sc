GenOrgCreature { 
	var <cells, <father, <mother, <lifespan;
	var <mates, <prey;
	var <age = 0, time, isKilled = false, <sex;	

	*new { | cells, father, mother, lifespan, mates, prey |
		^super.newCopyArgs(
			cells,
			father, 
			mother, 
			lifespan, 
			mates, 
			prey
		).initCreature;
	}

	initCreature { 
		time = Main.elapsedTime;
		sex = [\f, \m].choose;
		if(cells.isCollection.not, { cells = [cells] });
		if(mates.isCollection.not, { mates = [mates] });
		if(prey.isCollection.not, { prey = [prey] });
	}

	canMateWith { | creature |
		var bool = this.isRelativeOf(creature).not;
		bool = bool and: { creature.sex !=sex };
		if(0.1.coin, { this.switchSex });
		^(bool and: { mates.find([creature.class]).notNil} );
	}

	mateWith { | creature |
		if(this.canMateWith(creature), { 
			var target = creature.cells;
			var tsize = target.size;
			var newCells = cells.collect({ | cell, i |
				var tcell = target[i % tsize]; 
				cell.reproduceWith(tcell);
			}).select(_.notNil);
			var newSpan = lifespan + creature.lifespan 
			* 0.5 * rrand(0.9, 1.1);
			var newMates = mates, newPrey = prey;
			if(newMates!=creature.mates, { 
				newMates = [newMates, creature.mates].choose;
			});
			if(newPrey!=creature.prey, { 
				newPrey = [newPrey, creature.prey].choose;
			});
			^GenOrgCreature(
				newCells, 
				creature, 
				this, 
				newSpan, 
				newMates, 
				newPrey
			);
		});
		^nil;
	}

	eat { | creature |
		if(prey.find([creature.class]).notNil, { 
			var target = creature.size;
			var tsize = target.size; 
			cells = cells.collect({ | cell, i |
				cell.mutateWith(target[i % tsize]);
			});
			creature.kill;
		});
	}

	isParentOf { | creature |
		^(this==creature.father or: { this==creature.mother });
	}

	isSiblingOf { | creature |
		^(mother==creature.mother or: { father==creature.father });
	}

	isGrandOf { | creature |
		var bool = this.isParentOf(creature.father);
		^(bool || this.isParentOf(creature.mother))
	}

	isAuntOf { | creature |
		var bool = this.isSiblingOf(creature.mother);
		^(bool || this.isSiblingOf(creature.father));
	}

	isCousinOf { | creature |
		var bool = father.isAuntOf(creature);
		^(bool || mother.isAuntOf(creature));
	}

	isRelativeOf { | creature |
		var bool = this.isParentOf(creature);
		bool = bool || this.isSiblingOf(creature);
		bool = bool || this.isGrandOf(creature);
		bool = bool || this.isAuntOf(creature);
		^(bool || this.isCousinOf(creature));
	}

	lifespan_{ | newSpan |
		lifespan ?? { lifespan = newSpan };
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

	isDead { ^(age >= lifespan) }

	kill { age = lifespan * 2 }

	switchSex { 
		if(sex==\f, { sex = \m }, { sex = \f });
	}

	play { | timescale(1), out(0), target, addAction(\addToHead) |
		cells[0].playCell(timescale, out, target, addAction);
	}
}
