GenOrgCreature { 
	var cell, <father, <mother, <lifespan;
	var <age = 0, time, isKilled = false, <sex;	
	var <species;

	*new { | cell, father, mother, lifespan |
		^super.newCopyArgs(
			cell,
			father, 
			mother, 
			lifespan, 
		).initCreature;
	}

	initCreature { 
		time = Main.elapsedTime;
		sex = [\f, \m].choose;
	}

	canMateWith { | creature |
		var bool = this.isRelativeOf(creature).not;
		bool = bool and: { creature.sex !=sex };
		if(0.1.coin, { this.switchSex });
		^bool;
	}

	mateWith { | creature |
		if(this.canMateWith(creature), { 
			var target = creature.cell;
			var tsize = target.size;
			var newCells = cell.collect({ | cell, i |
				var tcell = target[i % tsize]; 
				cell.reproduceWith(tcell);
			}).select(_.notNil);
			var newSpan = lifespan + creature.lifespan * 0.5 * rrand(0.8, 1.2);
			^GenOrgCreature(
				newCells, 
				creature, 
				this, 
				newSpan, 
			);
		});
		^nil;
	}

	eat { | creature |
		var target = creature.size;
		var tsize = target.size; 
		cell = cell.collect({ | cell, i |
			cell.mutateWith(target[i % tsize]);
		});
		creature.kill;
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
		bool = bool || this.isCousinOf(creature);
		bool = bool || creature.isParentOf(this);
		bool = bool || creature.isGrandOf(this);
		bool = bool || creature.isAuntOf(this);
		^bool;
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

	playCreature { | timescale(1), out(0), target, addAction(\addToHead) |
		cell.playCell(timescale, out, target, addAction);
	}
}
