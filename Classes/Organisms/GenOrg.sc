GenOrg{
	// classvar oscBufferInfo;
	classvar goInstances;
	classvar server;

	var bufferReference, <behavior, <spatializer;
	var freeFunc, shouldDeleteBuffer;
	var <isInitialized = false;
	var mater, eater;

	*new{ |buffer, behavior, spatializer/*, playOnSpawn = false*/|
		var return;
		server = Server.default;

		goInstances = goInstances ? List.new;

		if(server.hasBooted==false){
			Error("Server is not booted!").throw;
		};

		return = super.new
		.pr_InitGenOrg(
			buffer, behavior,
			spatializer/*, playOnSpawn*/
		);

		goInstances.add(return);

		^return;
	}

	buffer{
		^bufferReference.value;
	}

	pr_InitGenOrg{|bf, be, sp/*, bool*/|
		var setResourcesFunc;
		if(bf.class!=Ref and: {bf.class!=Buffer}){
			if(bf.class==Float){
				bf = bf.asInteger;
			};
			if(bf.class!= Integer){
				Error("GenOrg lacks a valid buffer input.").throw;
			};
		};
		if(bf.class==Buffer){
			if(bf.numChannels==2){
				bf = Buffer.readChannel(server, bf.path, channels: 0);
			};
		};
		if(bf.class!=Ref){
			bf = `bf;
		};
		bf.value.normalize;
		bufferReference = bf;
		setResourcesFunc = {
			if(sp.isSpatialCell){
				spatializer = sp;
			}/*ELSE*/{
				if(sp.class==Symbol or: {sp.class==String}){
					case
					{sp.asString.toLower=="Binaural".toLower}{
						spatializer = SpatialCellBinaural.new;
					}
					{sp.asString.toLower=="FOA".toLower}{
						spatializer = SpatialCellFOA.new;
					}
					{sp.asString.toLower=="HOA".toLower}{
						spatializer = SpatialCellHOA.new;
					};
				}/*ELSE*/{
					if(sp.isNil){
						spatializer = SpatialCellMono.new;
						server.sync;
					}/*ELSE*/{
						Error("Invalid spatializer input").throw;
					};
				};
			};
			behavior = be ? GenOrg_Behavior.new;
			if(isInitialized==false){
				freeFunc = `nil;
				shouldDeleteBuffer = `false;
				spatializer.onFree({
					this.pr_FreeOrganism(shouldDeleteBuffer.value);
					if(freeFunc.value.isNil.not){
						freeFunc.value.value;
					};
				});
				isInitialized = true;
			};
		};

		if(sp.isNil || be.isNil){
			forkIfNeeded{
				setResourcesFunc.value;
			};
		}/*ELSE*/{
			setResourcesFunc.value;
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

	mate{|organism|
		var newOrganism = `(nil);
		if(organism.isNil.not){
			if(organism.isGenOrg){
				if(this.buffer.bufnum.isNil.not
					&& organism.buffer.bufnum.isNil.not){
					var newBehavior, newBuffer, newSpatializer;
					newBehavior = GenOrg_Mutator
					.mateBehaviors(behavior, organism.behavior);
					newBuffer = GenOrg_Mutator
					.mateBuffers(this.buffer, organism.buffer, {
						newSpatializer = spatializer.class.new;
						newOrganism.value = GenOrg
						.new(newBuffer, newBehavior, newSpatializer);
						// "Organism delivered!".postln;
					});
				};
			};
		};

		^newOrganism;
	}

	eat{|organism|
		if(this.buffer.isNil.not){
			if(organism.isGenOrg){
				if(organism.buffer.isNil.not){
					if(organism.buffer.bufnum.isNil.not){
						bufferReference =  GenOrg_Mutator
						.eatBuffers(this.buffer, organism.buffer);
					};
				};
			};
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
		shouldDeleteBuffer.value = deleteBuffer;
		if(spatializer.isFreed==false){
			spatializer.free;
		}/*ELSE*/{
			this.pr_FreeOrganism(shouldDeleteBuffer.value);
			freeFunc.value;
		};
	}

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

	isGenOrg{
		^true;
	}

	*deleteOrganismFiles{
		GenOrg_Mutator.deleteOrganismFiles;
	}

	*instances{
		^goInstances;
	}

}

+ Object{
	isGenOrg{
		^false;
	}
}


