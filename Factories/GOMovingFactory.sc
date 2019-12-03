GOMovingFactory : GenerativeOrganismFactory{

	var incubator, garbage;

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

	pr_ConnectMover{ |organismReference|

		fork{

			var mover = SpaceCellMover.new(
				azimuthRate: exprand(0.1, 2.0),
				elevationRate: exprand(0.1, 2.0),
				distanceRate: exprand(0.1, 2.0)
			);

			server.sync;
			mover.mapTo(organismReference.value.spatializer);

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

							var orgRef =incubator.removeAt(key);

							this.pr_ConnectMover(orgRef);

							this.pr_AddOrganism(key, orgRef);
							if(widgetToRegisterWith.isNil.not){
								widgetToRegisterWith.registerOrganism(key);
							}

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
			var organism = organisms.removeAt(inputSymbol);

			garbage = garbage ? Dictionary.new;
			garbage.add(inputSymbol -> organism);
		};

	}

	killGarbage{

		if(garbage.isNil.not){
			if(garbage.size > 0){

				garbage.copy.keys.do{|key|
					garbage.removeAt(key).value.free(deleteFiles);
				}

			}
		};

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
		super.free;

		if(garbage.isNil.not){
			garbage.do{|reference|
				reference.value.free;
			}
		};

		garbage.clear;
		garbage = nil;

		if(incubator.isNil.not){
			incubator.do{|reference|
				var item = reference.value;
				if(item.isNil.not){

					if(item.isGenerativeOrganism){
						item.value.free;
					};
				};
			}
		};

		incubator.clear;
		incubator = nil;
	}

}