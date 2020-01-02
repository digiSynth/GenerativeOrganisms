GOAgent : GenerativeOrganism{
	classvar <>timeToMateLo = 0.0625, <>timeToMateHi = 0.35;
	classvar <>timeToEatLo = 0.03125, <>timeToEatHi = 0.125;
	classvar <>hitPointsLo = 2, <>hitPointsHi = 16;
	classvar <>lifespanLo = 20.0, <>lifespanHi = 60.0;

	var startTime, currentTime, previousTime, lastCheckedTime;
	var <timeSinceEaten = 0, <timeSinceMated = 0;
	var <age = 0.0, timeToMate = 100000, timeToEat = 100000, <>hitPoints = 100000;
	var lifespan = 100000, <lifespanScalar = 1, startingHitPoints;

	var lifespanCanSet = true, timeToMateCanSet = true, timeToEatCanSet = true;

	*new{ |buffer, behavior, spatializer|
		^super.new(buffer, behavior, spatializer).pr_InitGOAgent;
	}

	pr_InitGOAgent{

		lifespan = exprand(lifespanLo, lifespanHi);
		timeToMate = lifespan * exprand(timeToMateLo, timeToMateHi);
		timeToEat = lifespan * exprand(timeToEatLo, timeToEatHi);
		startingHitPoints = exprand(hitPointsLo, hitPointsHi).asInteger;
		hitPoints = startingHitPoints;

		previousTime = currentTime = startTime = Main.elapsedTime;
		lastCheckedTime = timeSinceEaten = timeSinceMated = 0;

	}

	lifespanScalar_{|newValue|

		if(newValue > 1){

			lifespanScalar = newValue;

		}/*ELSE*/{

			lifespanScalar = 1;

		};

	}

	lifespan{
		^(lifespan * lifespanScalar);
	}

	pr_lifespan{

		^lifespan;

	}

	pr_lifespan_{

		|newVal|

		if(lifespanCanSet){

			lifespan = newVal;
			lifespanCanSet = false;

		};

	}

	pr_startingHitPoints{

		^startingHitPoints;

	}

	pr_startingHitPoints_{|newVal|

		if(newVal.class==Integer or: {newVal.class==Float}){

			startingHitPoints = newVal;
			hitPoints = startingHitPoints;

		};

	}

	timeToMate{

		^(timeToMate *lifespanScalar);

	}

	pr_timeToMate{

		^timeToMate;

	}

	pr_timeToMate_{|newVal|

		if(timeToMateCanSet){

			timeToMate = newVal;
			timeToMateCanSet = false;

		}

	}

	timeToEat{

		^(timeToEat * lifespanScalar);
	}

	pr_timeToEat{

		^timeToEat;

	}

	pr_timeToEat_{|newVal|

		if(timeToEatCanSet){

			timeToEat = newVal;
			timeToEatCanSet = false;

		};

	}

	updateGOAgent{

		if(isInitialized){
			currentTime = Main.elapsedTime;
			age = currentTime - startTime;

			lastCheckedTime =  currentTime - previousTime;
			timeSinceEaten = timeSinceEaten + lastCheckedTime;
			timeSinceMated = timeSinceMated + lastCheckedTime;

			previousTime = Main.elapsedTime;
		};
	}

	eat{|organism|

		super.eat(organism);

		hitPoints = hitPoints + [1, 2, 3, 4, 5]
		.wchoose([5, 4, 3, 2, 1].normalizeSum);

		timeSinceEaten = 0.0;


	}

	mate{|organism|
		var newOrganism = `(nil);

		if(organism.isNil.not){

			if(organism.isGenerativeOrganism){

				if(organism.buffer.isNil.not && this.buffer.isNil.not){

					var newBehavior, newBuffer, newSpatializer;

					newBehavior = GenerativeMutator
					.mateBehaviors(behavior, organism.behavior);

					newBuffer = GenerativeMutator
					.mateBuffers(this.buffer, organism.buffer, {
						var organismToDeliver;

						newSpatializer = spatializer.class.new;

						organismToDeliver = GOAgent
						.new(newBuffer, newBehavior, newSpatializer);

						organismToDeliver.pr_lifespan =
						(this.pr_lifespan + organism.pr_lifespan) / 2;

						organismToDeliver.pr_timeToMate =
						(this.pr_timeToMate + organism.pr_timeToMate) / 2;

						organismToDeliver.pr_timeToEat =
						(this.pr_timeToEat + organism.pr_timeToEat) / 2;

						organism.pr_startingHitPoints =
						((this.pr_startingHitPoints + organism.pr_startingHitPoints) / 2)
						.ceil.asInteger;

						newOrganism.value = organismToDeliver;

					});


				};

			};

		};

		^newOrganism;
	}



}