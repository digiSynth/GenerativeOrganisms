GOMovingSystem{

	classvar server;
	classvar <deleteFiles = false;

	var <factory, <isRunning = false;
	var quitRegistered = false;
	var isDead = false;

	var updateTask;
	var waitTimeReference, chanceToChangeWaitRef = 0.2;
	var widget, populatingRoutine;

	*new{|bufferArray, behaviorArray|

		server = server ? Server.default;

		if(server.hasBooted==false){
			Error("Server has not booted!").throw;
		};

		if(bufferArray.isNil){
			Error("System requires an array of buffers.").throw;
		};

		SpaceCell.loadSpaceCellSynthDefs;


		^super.new.pr_InitGOMovingSystem(bufferArray, behaviorArray);

	}

	pr_InitGOMovingSystem{|ba, bea|
		if(quitRegistered==false){
			ServerQuit.add({
				try{this.free;};
			});
			quitRegistered = true;
		};

		factory = GOMovingFactory.new(ba, bea);

		waitTimeReference = waitTimeReference ? `(1);

		widget = GOMovingSystemWidget.new(this);

		isRunning = true;
	}

	start{
		this.pr_PopulateFactory
	}

	pr_PopulateFactory{

		if(populatingRoutine.isNil.not){
			if(populatingRoutine.isPlaying){
				populatingRoutine.stop;
			}
		};

		populatingRoutine = fork{

			block{|break|


				factory.buffers.size.do{|index|

					if(isDead==false){

						factory.spawn(index, index);

						widget.registerOrganism(index.asSymbol);
						widget.log(format("Organism % is born", index));

						exprand(0.05, 8.0).wait;

						factory.sound(index.asSymbol, 5);

					}/*ELSE*/{

						break.value(999);

					};

				};

			};

		};

		this.pr_StartSimulation;
	}

	pause{

		if(updateTask.isNil.not){
			updateTask.pause;
		};

	}

	resume{

		if(updateTask.isNil.not){
			updateTask.resume;
		};

	}

	play{
		this.resume;
	}

	pr_StartSimulation{

		if(updateTask.isNil.not){
			updateTask.stop;
		};

		updateTask = Task({

			server.sync;

			loop{
				// var chanceToChangeWaitRef = 0.1;
				var organismCount = 0;
				var organisms = factory.organisms;

				if(organisms.size==0){
					this.pr_EverythingIsDead;
				}/*ELSE*/{

					organisms.keys.asArray.scramble.do{|key|
						var orgRef = organisms[key];
						var organism = orgRef.value;

						if(organism.isGenerativeOrganism)
						{
							organism.updateGOAgent;

							organism.lifespanScalar = waitTimeReference.value;

							organism.behavior.options.timescaleScalar =
							waitTimeReference.value;

							if((organism.age >= organism.lifespan) or:
								{organism.hitPoints<=0}){

								factory.sound(key, 6);

								widget.log(format("Organism % is killed", key));

								factory.kill(key);

								widget.removeAt(key);

							}/*ELSE*/{

								var isStarving = false, isLookingToMate = false;

								if(organism.timeSinceEaten > organism.timeToEat){
									var chanceToEat = rrand(1/4, 3.5/4);

									if(chanceToEat.coin){

										var organismKeyToEat = organisms.keys.choose;

										if(key!=organismKeyToEat){

											fork{

												factory.sound(key, 4);


												widget.log(format("Predator % eats prey %",
													key, organismKeyToEat
												));

												factory.eatOrganisms(key, organismKeyToEat);

												widget.removeAt(organismKeyToEat);

											};

										} /*ELSE*/{

											organism.hitPoints = organism.hitPoints - 1;
											/*widget.log(format("Organism % is starving", key));

											factory.sound(key, 2);*/

											isStarving = true;

										};

									}/*ELSE*/{

										organism.hitPoints = organism.hitPoints - 1;

										/*widget.log(format("Organism % is starving", key));

										factory.sound(key, 2);*/

										isStarving = true;

									}
								};

								if(organism.timeSinceMated > organism.timeToMate){

									var chanceToMate = exprand(0.5, 0.875);

									if(chanceToMate.coin){

										var organismToMateWith = organisms.keys.choose;

										if(organismToMateWith!=key){

											var uniqueID = UniqueID.next.asSymbol;

											factory.sound(key, 0);

											widget.log(format("Organisms % mates with organism %. \n\tTheir child is %.",
												key, organismToMateWith, uniqueID
											));

											factory.mateOrganisms(key, organismToMateWith, uniqueID);


										}/*ELSE*/{

											isLookingToMate = true;
											/*
											widget.log(format("Organism % is looking to mate", key));

											factory.sound(key, 1);*/

										}

									}/*ELSE*/{

										isLookingToMate = true;

										/*widget.log(format("Organism % is looking to mate", key));

										factory.sound(key, 1);
										*/
									};

								};

								case
								{isStarving && isLookingToMate}{
									var chosenFunction = [
										{
											widget.log(format("Organism % is looking to mate", key));

											factory.sound(key, 1);
										},

										{
											widget.log(format("Organism % is starving", key));

											factory.sound(key, 2);
										}

									].choose;

									chosenFunction.value;
								}

								{isStarving && isLookingToMate==false}{


									widget.log(format("Organism % is starving", key));

									factory.sound(key, 2);


								}


								{isLookingToMate && isStarving==false}{

									widget.log(format("Organism % is looking to mate", key));

									factory.sound(key, 1);
								}

								{isLookingToMate==false && isStarving==false}{

									if(rrand(0.1, 1.0).rand.coin){
										widget.log(format("Organism % makes some noise", key));

										factory.sound(key, 3);

									};

								};

							};

						};

						waitTimeReference.value.wait;
						this.pr_UpdateWaitReference;

						factory.transferToOrganisms(widget);
						factory.killGarbage;

						block{|break|

							organisms.do{|ref|

								var item = ref.value;

								if(item.isGenerativeOrganism){

									organismCount = organismCount + 1;
									break.value(999);

								};

							};


							if(organismCount == 0){
								this.pr_EverythingIsDead;
							};

						}/*ELSE*/{
							this.pr_EverythingIsDead;
						};



					}
				};

			}/*ELSE*/{

				this.pr_EverythingIsDead;

			};

		}).play;
	}


	pr_UpdateWaitReference{
		var val = waitTimeReference.value;

		if(chanceToChangeWaitRef.coin){

			val ?? {
				waitTimeReference.value = 1.0;
			};

			case
			{val < (1/5)}{

				waitTimeReference.value = val * rrand(0.95, 1.5);
				chanceToChangeWaitRef = rrand(0.75, 1.0);

			}

			{val > 4.5}{

				waitTimeReference.value = val * rrand(0.25, 1.05);
				chanceToChangeWaitRef = rrand(0.75, 1.0);
			}

			{(val > (1/5) and: {val < 4.5})}{

				waitTimeReference.value = val * rrand(0.5, 1.5);
				chanceToChangeWaitRef = exprand(0.1, 0.75);

			};

		};

	}

	pr_EverythingIsDead{
		widget.log("Everything is dead!");
		isDead = true;

		if(populatingRoutine.isNil.not){

			if(populatingRoutine.isPlaying){

				populatingRoutine.stop;
				populatingRoutine = nil;

			}
		};

		if(factory.organisms.isEmpty.not){
			factory.organisms.keys.do{|key|
				widget.removeAt(key);
			}
		};

		factory.organisms.clear;
		GOAgent.freeAll;
		updateTask.stop;
	}

	updateDur{

		^waitTimeReference.value;

	}

	updateDur_{|newValue|

		if(waitTimeReference.value.isNil.not){

			waitTimeReference.value = newValue;

		}/*ELSE*/{

			waitTimeReference = `newValue;

		};

	}

	free{

		if(updateTask.isNil.not){
			updateTask.stop;
		};

		factory.free;

		isRunning = false;
	}

	*spatializerClass_{|newClass|

		GenerativeOrganismFactory.spatializerClass = newClass;

	}

	*spatializerClass{

		^GenerativeOrganismFactory.spatializerClass;

	}

}