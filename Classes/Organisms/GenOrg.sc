GenOrg{
	// classvar oscBufferInfo;
	classvar goInstances;
	classvar server;

	var bufferReference, <behavior, <spatializer;
	var freeFunc, shouldDeleteBuffer;
	var <isInitialized = false;
	var mater, eater;
	var matingSynthDef, eatingSynthDef;

	*new{ |buffer, behavior, spatializer/*, playOnSpawn = false*/|
		var return;
		server = Server.default;

		goInstances = goInstances ? List.new;

		if(server.hasBooted==false){
			Error("Server is not booted!").throw;
		};

		return = super.new
		.buffer_(buffer)
		.behavior_(behavior)
		.spatializer_(spatializer)
		.pr_InitGenOrg;

		goInstances.add(return);

		^return;
	}

	//Get and set fields
	buffer{
		^bufferReference.value;
	}

	buffer_{|newBuffer|

		if(newBuffer.isKindOf(Ref).not and: {newBuffer.isKindOf(Buffer).not}){
			if(newBuffer.isKindOf(Float)){
				newBuffer = newBuffer.asInteger;
			};
			if(newBuffer.isKindOf(Integer).not){
				Error("GenOrg lacks a valid buffer input.").throw;
			};
		};

		if(newBuffer.isKindOf(Buffer)){
			if(newBuffer.numChannels==2){
				newBuffer = Buffer.readChannel(server, newBuffer.path, channels: 0);
			};
		};

		if(newBuffer.isKindOf(Ref).not){
			newBuffer = `newBuffer;
		}/*ELSE*/{
			if(newBuffer.value.isKindOf(Buffer).not){
				Error("GenOrg lacks a valid buffer input.").throw;
			};
		};

		newBuffer.value.normalize(0.8);
		bufferReference = newBuffer;

	}

	spatializer_{|newSpatializer|

		if(newSpatializer.isSpatialCell){
			spatializer = newSpatializer;
		}/*ELSE*/{
			if(newSpatializer.class==Symbol or: {newSpatializer.class==String}){
				case
				{newSpatializer.asString.toLower=="Binaural".toLower}{
					spatializer = SpatialCellBinaural.new;
				}
				{newSpatializer.asString.toLower=="FOA".toLower}{
					spatializer = SpatialCellFOA.new;
				}
				{newSpatializer.asString.toLower=="HOA".toLower}{
					spatializer = SpatialCellHOA.new;
				};
			}/*ELSE*/{

				if(newSpatializer.isNil){
					spatializer = SpatialCellMono.new;
				}/*ELSE*/{
					Error("Invalid spatializer input").throw;
				};
			};
		};
	}

	behavior_{|newBehavior|
		if(newBehavior.isNil){
			behavior = GenOrg_Behavior.new;
		}/*ELSE*/{

			if(newBehavior.isKindOf(GenOrg_Behavior)){
				behavior = newBehavior;
			}/*ELSE*/{
				Error("GenOrg lacks a valid behavior input.").throw;
			}

		};

	}

	playGenOrg{|db = -3|
		if(isInitialized){
			spatializer.playSpatialCell;
			behavior.playBehavior(this.buffer, db,
				spatializer.inputBus, spatializer.group, 'addToHead');
		};
	}

	position{|azimuth = 0, elevation = 0, distance = 1.0|
		spatializer.playSpatialCell;
		spatializer.azimuth = azimuth;
		spatializer.elevation = elevation;
		spatializer.distance = distance;
	}

	//Mutation methods
	mate{|organism|
		var newOrganism = `(nil);
		if(mater.isNil){
			this.matingSynthDef_(matingSynthDef);
		};

		if(organism.isNil.not){
			if(organism.isKindOf(GenOrg)){

				if(this.buffer.bufnum.isNil.not
					&& organism.buffer.bufnum.isNil.not){

					var newBehavior, newBuffer, newSpatializer;

					newSpatializer = spatializer.class.new;
					newBehavior = behavior.averageBehaviors(organism.behavior);
					newBuffer = mater.mutate(this.buffer, organism.buffer, 1, {
						newOrganism.value = GenOrg.new(
							newBuffer,
							newBehavior,
							newSpatializer
						);
					});

				};
			};
		};

		^newOrganism;
	}

	eat{|organism|
		if(eater.isNil){
			this.eatingSynthDef_(eatingSynthDef);
		};

		if(this.buffer.isNil.not){
			if(organism.isGenOrg){
				if(organism.buffer.isNil.not){

					if(organism.buffer.bufnum.isNil.not){

						bufferReference = eater.mutate(this.buffer, organism.buffer);

					};

				};
			};
		};
	}

	matingSynthDef_{|newSynthDef|
		if(mater.isNil){
			mater = GenOrg_Mutator.new(newSynthDef);
		}/*ELSE*/{
			matingSynthDef = newSynthDef;
			mater.synthDef_(newSynthDef);
		};
	}

	eatingSynthDef_{|newSynthDef|
		if(eater.isNil){
			eater = GenOrg_Mutator.new(newSynthDef);
		}/*ELSE*/{
			eatingSynthDef = newSynthDef;
			eater.synthDef_(newSynthDef);
		};
	}

	*freeAll{
		if(goInstances.isNil.not and: {goInstances.size > 0}){
			goInstances.copy.do{|item|
				item.free;
			};
		};
	}

	onFree{|function|
		if(freeFunc.isNil){
			freeFunc = `function;
		}/*ELSE*/{
			freeFunc.value = freeFunc.value ++ function;
		};
	}

	free{|deleteBuffer = false|
		if(spatializer.isFreed==false){
			spatializer.free;
		}/*ELSE*/{
			this.pr_FreeOrganism(deleteBuffer);
			this.pr_EvaluateFreeFunc;
		};

	}

	lag{
		if(spatializer.isNil.not){
			^spatializer.lag;
		}/*ELSE*/{
			^nil;
		};
	}

	lag_{ |newLag|
		if(spatializer.isNil.not){
			spatializer.lag = newLag;
		};
	}

	*instances{
		^goInstances;
	}

	//Private methods
	pr_FreeOrganism{|deleteBuffer = false|
		isInitialized = false;
		behavior.free;
		super.free;
		goInstances !? {
			goInstances.remove(this);
		};

		if(deleteBuffer){
			File.delete(bufferReference.value.path);
		};
	}

	pr_InitGenOrg{

		if(isInitialized==false){
			// freeFunc = `nil;
			shouldDeleteBuffer = false;
			spatializer.onFree({
				this.pr_FreeOrganism(shouldDeleteBuffer);
				this.pr_EvaluateFreeFunc;
			});

			isInitialized = true;
		};

	}

	pr_EvaluateFreeFunc{
		if(freeFunc.isNil.not){
			if(freeFunc.isFunction){
				freeFunc.value;
			}/*ELSE*/{
				if(freeFunc.isKindOf(Ref)){
					var value = freeFunc.value;
					if(value.isFunction){
						value.value;
					};
				};
			};
		};
	}

}