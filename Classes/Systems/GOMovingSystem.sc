GOMovingSystem{

	classvar server;
	classvar <deleteFiles = false;

	var <durLo, <durHi, <durStep;
	var waitingRoutine;
	var <factory, <isRunning = false;
	var quitRegistered = false;
	var isDead = false;

	var updateTask;
	var <widget, populatingRoutine;

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

		durLo = durLo ? server.latency;
		durHi = durHi ? (server.latency * 8);
		durStep = durStep ? (durLo * 0.25);

		waitingRoutine = waitingRoutine ? `(Pbrown(
			durLo,
			durHi,
			durStep,
			inf
		).asStream);

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

		populatingRoutine = Task({

			block{|break|

				factory.buffers.copy.size.do{|index|

					if(isDead==false){

						factory.spawn(index, index);

						widget.registerOrganism(index.asSymbol);
						widget.log(format("Organism % is born", index));

						server.sync;

						factory.sound(index.asSymbol, 5);

						if(index > 4){

							exprand(0.05, 8.0).wait;

						}/*ELSE*/{

							exprand(0.05, 2.5).wait;

						};

					}/*ELSE*/{

						break.value(999);

					};

				};

			};

		});

		populatingRoutine.play;

		this.pr_StartSimulation;
	}

	pause{

		if(updateTask.isNil.not){

			if(updateTask.isPlaying){

				updateTask.pause;

			};

		};


		if(populatingRoutine.isNil.not){

			if(populatingRoutine.isPlaying){

				populatingRoutine.pause;

			}

		};

	}

	resume{

		if(updateTask.isNil.not){

			if(updateTask.isPlaying.not){

				updateTask.play;

			};

		};


		if(populatingRoutine.isNil.not){

			if(populatingRoutine.isPlaying.not){

				populatingRoutine.play;

			}

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

			var checkForDead = 8;
			var timeToCheckForDead = 0;

			1.0.wait;
			server.sync;

			loop{
				var timeToWait = waitingRoutine.value.next;
				var organismCount = 0;
				var organisms = factory.organisms;

				if(organisms.size==0){

					this.pr_EverythingIsDead;

				}/*ELSE*/{

					var osize = organisms.size;

					organisms.keys.copy.do{|key|

						var orgRef = organisms[key];
						var organism = orgRef.value;

						if(organism.isGenerativeOrganism)
						{
							organism.updateGOAgent;

							organism.lifespanScalar = timeToWait;

							organism.behavior.options.timescaleScalar =
							timeToWait;

							if((organism.age >= organism.lifespan) or:
								{organism.hitPoints<=0}){

								factory.sound(key, 6);

								widget.log(format("Organism % is killed", key));

								factory.kill(key);

								widget.removeAt(key);

							}/*ELSE*/{

								var isStarving = false, isLookingToMate = false, hadChild = false;

								if(organism.timeSinceEaten > organism.timeToEat){
									var chanceToEat = rrand(0.3, 0.8);

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

											isStarving = true;

										};

									}/*ELSE*/{

										organism.hitPoints = organism.hitPoints - 1;

										isStarving = true;

									}
								};

								if(organism.timeSinceMated > organism.timeToMate){

									var chanceToMate = exprand(0.5, 0.875);

									if(chanceToMate.coin){

										var organismToMateWith = organisms.keys.choose;

										if(organismToMateWith!=key){

											var uniqueID = UniqueID.next.asSymbol;

											hadChild = true;

											factory.sound(key, 0);

											widget.log(format("Organisms % mates with organism %. "
												++"\n\tTheir child is %.",
												key, organismToMateWith, uniqueID
											));

											factory.mateOrganisms(key, organismToMateWith, uniqueID);

											0.05.wait;

										}/*ELSE*/{

											isLookingToMate = true;

										};

									}/*ELSE*/{

										isLookingToMate = true;

									};

								};

								if(hadChild==false){

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

						};

						timeToWait.wait;

						factory.transferToOrganisms(widget);

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

					};

					timeToCheckForDead = timeToCheckForDead + 1;

					if(timeToCheckForDead==checkForDead){

						widget.log("\nChecking for dead organisms");

						organisms.keys.copy.do{|orgRefKey|

							var organism =  organisms[orgRefKey].value;

							if(organism.age >= organism.lifespan){

								factory.sound(orgRefKey, 6);

								widget.log(
									format("\t\tOrganism % is found dead", orgRefKey)
								);

								factory.kill(orgRefKey);

								widget.removeAt(orgRefKey);

							};

						};

						widget.log("");
						timeToCheckForDead = 0;

					}

				};


			};

		}).play;

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
			};

		};

		factory.organisms.clear;
		GOAgent.freeAll;
		updateTask.stop;

	}


	durLo_{|newDur = 0.1|

		if(newDur <= durHi){

			durLo = newDur;

		}/*ELSE*/{

			durLo = durHi;

		};

		if(durStep > durLo){

			durStep = durLo;

		};

		waitingRoutine.value = Pbrown(
			durLo,
			durHi,
			durStep,
			inf
		).asStream;

	}

	durHi_{|newDur = 4.0|

		if(newDur >= durLo){

			durHi = newDur;

		}/*ELSE*/{

			durHi = durLo;

		};

		waitingRoutine.value = Pbrown(
			durLo,
			durHi,
			durStep,
			inf
		).asStream;

	}

	durStep_{|newStep = 0.1|

		if(newStep <= durLo){

			durStep = newStep;

		}/*ELSE*/{

			durStep = durLo;

		};

		waitingRoutine.value = Pbrown(
			durLo,
			durHi,
			durStep,
			inf
		).asStream;

	}

	free{

		if(populatingRoutine.isNil.not){

			if(populatingRoutine.isPlaying){
				populatingRoutine.stop;
				populatingRoutine = nil;
			}

		};

		if(updateTask.isNil.not){
			if(updateTask.isPlaying){
				updateTask.stop;
			};
			updateTask = nil;
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