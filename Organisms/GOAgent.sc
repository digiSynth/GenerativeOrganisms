GOAgent : GenerativeOrganism{
	classvar <>timeToMateLo = 0.0625, <>timeToMateHi = 0.35;
	classvar <>timeToEatLo = 0.03125, <>timeToEatHi = 0.125;
	classvar <>hitPointsLo = 2, <>hitPointsHi = 16;
	classvar <>lifespanLo = 20.0, <>lifespanHi = 60.0;

	var startTime, currentTime, previousTime, lastCheckedTime;
	var <timeSinceEaten = 0, <timeSinceMated = 0;
	var <age = 0.0, timeToMate = 100000, timeToEat = 100000, <>hitPoints = 100000;
	var lifespan = 100000, <lifespanScalar;

	*new{ |buffer, behavior, spatializer|
		^super.new(buffer, behavior, spatializer).pr_InitGOAgent;
	}


	pr_InitGOAgent{

		lifespan = exprand(lifespanLo, lifespanHi);
		timeToMate = lifespan * exprand(timeToMateLo, timeToMateHi);
		timeToEat = lifespan * exprand(timeToEatLo, timeToEatHi);
		hitPoints = exprand(hitPointsLo, hitPointsHi).asInteger;

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

	timeToMate{

		^(timeToMate *lifespanScalar);
	}

	timeToEat{

		^(timeToEat * lifespanScalar);
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

						newSpatializer = spatializer.class.new;

						newOrganism.value = GOAgent
						.new(newBuffer, newBehavior, newSpatializer);
						// "Organism delivered!".postln;

					});


				};

			};

		};

		^newOrganism;
	}



}