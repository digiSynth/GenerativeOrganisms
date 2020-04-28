GOMovingFactory : GenerativeOrganismFactory{

	var incubator, garbage, garbageCollectorRoutine;

	var toSound, soundRoutine;

	var toConnect, connectionRoutine;

	spawn{|inputSymbol, bufferIndex, behaviorIndex|

		if(inputSymbol.class!=Symbol){
			try{inputSymbol = inputSymbol.asSymbol};
		};

		organisms[inputSymbol] ?? {
			var organismToAdd
			= this.pr_MakeOrganism(bufferIndex, behaviorIndex);

			organisms.add(inputSymbol -> organismToAdd);

			this.pr_ConnectMover(organismToAdd);
		};

	}

	sound{|index, type|

		toSound = toSound ? List.new;

		this.pr_RunSoundRoutine([index, type]);

	}

	pr_RunSoundRoutine{|toAdd|

		if(soundRoutine.isPlaying){

			soundRoutine.stop;

		};

		toSound.add(toAdd);


		soundRoutine = Routine({

			if(toSound.isEmpty.not){

				toSound.copy.size.do{

					if(toSound.isEmpty.not){

						var arr = toSound.removeAt(0);

						super.sound(arr[0], arr[1]);

						(1/100).wait;

					};

				};

			};


		});

		soundRoutine.play;


	}

	pr_ConnectMover{ |organismReference|

		toConnect = toConnect ? List.new;

		toConnect.add(organismReference);

		this.pr_RunConnectionRoutine;

	}

	pr_RunConnectionRoutine{

		if(connectionRoutine.isPlaying){

			connectionRoutine.stop;

		};

		if(toConnect.isEmpty.not){
			connectionRoutine = Routine({

				if(toConnect.isEmpty.not){

					loop{

						toConnect.copy.size.do{

							var orgRef = toConnect.removeAt(0);

							if(
								orgRef.value.isGenerativeOrganism
								and: {orgRef.value.spatializer.synth.isPlaying}
							){

								var mover;

								server.sync;

								mover = SpaceCellMover.new(
									azimuthRate: exprand(0.1, 2.0),
									elevationRate: exprand(0.1, 2.0),
									distanceRate: exprand(0.1, 2.0)
								);

								server.sync;

								mover.mapTo(orgRef.value.spatializer);

							}/*ELSE*/{

								toConnect.add(orgRef);

							};


						};

						server.sync;
						server.latency.wait;

						if(toConnect.isEmpty){

							thisThread.stop;
							server.latency.wait;

						};

					};

				};

			});

			connectionRoutine.play;

		};

	}

	addOrganism{|inputSymbol, organism|

		if(organism.class!=Ref){
			organism = `organism;
		};

		incubator = incubator ? Dictionary.new;
		incubator.add(inputSymbol.asSymbol -> organism);
	}

	pr_AddOrganism{|inputSymbol, organism|

		if(organism.class!=Ref){
			organism = `organism;
		};

		organisms = organisms ? Dictionary.new;
		organisms.add(inputSymbol.asSymbol -> organism);
	}

	transferToOrganisms{|widgetToRegisterWith|

		if(incubator.isNil.not){
			if(incubator.size > 0){

				incubator.keys.do{|key|
					var incubated = incubator[key].value;

					if(incubated.isNil.not){

						if(incubated.isGenerativeOrganism){

							var orgRef = incubator.removeAt(key);

							this.pr_ConnectMover(orgRef);

							this.pr_AddOrganism(key, orgRef);

							this.sound(key, 5);

							if(widgetToRegisterWith.isNil.not){
								widgetToRegisterWith.registerOrganism(key);
							};

						}
					}

				}

			}
		}

	}

	kill{|inputSymbol|

		if(inputSymbol.class!=Symbol){
			try{inputSymbol = inputSymbol.asSymbol};
		};

		organisms[inputSymbol].value !? {
			var orgReference = organisms.removeAt(inputSymbol);

			this.pr_RunGarbageCollectionRoutine(orgReference);
		};

	}

	pr_RunGarbageCollectionRoutine{|orgReference|

		if(garbageCollectorRoutine.isPlaying){

			garbageCollectorRoutine.pause;

		};

		garbage = garbage ? List.new;
		garbage.add(orgReference);

		if(garbageCollectorRoutine.isPlaying){

			garbageCollectorRoutine.stop;

		};

		garbageCollectorRoutine = Routine({

			if(garbage.isEmpty.not){

				garbage.copy.size.do{

					if(garbage.isEmpty.not){

						garbage.removeAt(0).value.free;

						(1/100).wait;

					};

				};
			};

		});


		garbageCollectorRoutine.play;
	}

	pr_MakeOrganism{|bufferIndex, behaviorIndex|

		var organism;
		var spatializer = spatializerClass.new;
		var buffer, behavior;

		bufferIndex = bufferIndex !?
		{bufferIndex.asInteger} ? {(buffers.size - 1).rand;};

		if(behaviorIndex.isNil.not and: {behaviors.isNil.not}){
			if(behaviorIndex >= behaviors.size){
				behavior = nil;
			}/*ELSE*/{
				behavior = behaviors.removeAt(behaviorIndex.asInteger);
			};
		}/*ELSE*/{
			behavior = nil;
		};

		if(bufferIndex >= buffers.size){
			bufferIndex = (buffers.size - 1).rand;
		};

		buffer = buffers[bufferIndex];

		organismCount = organismCount + 1;

		organism = GOAgent.new(buffer,
			behavior, spatializer);

		^`organism;
	}

	free{

		if(connectionRoutine.isPlaying){

			connectionRoutine.stop;

		};

		toConnect.clear;
		// toConnect.clear;
		// connectionRoutine = nil;

		if(soundRoutine.isPlaying){

			soundRoutine.stop;

		};

		// toSound.clear;
		// soundRoutine = nil;

		super.free;

		if(incubator.isNil.not){

			incubator.copy.size.do{

				var item = incubator.removeAt(0).value;

				if(item.isGenerativeOrganism){

					item.free;

				};
			};

		};

		// incubator = nil;

	}


}