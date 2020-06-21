GenOrg {    
	var moduleSet, from, buffer, freeFunc; 
	var server, <mater, <eater, <behavior, <spatializer; 

	*new { | spatializerType(\mono), moduleSet(\default), from | 
		^super.newCopyArgs(moduleSet, from).initGenOrg(spatializerType);
	}

	modulSet_{ | newModuleSet, newFrom | 
		spatializer.moduleSet_(newModuleSet, newFrom); 
		behavior.moduleSet_(newModuleSet, newFrom); 
		eater.moduleSet_(newModuleSet, newFrom); 
		mater.moduleSet_(newModuleSet, newFrom);
		moduleSet = newModuleSet; 
		from = newFrom;
	}

	*basicNew { ^super.new; }

	initGenOrg { | type | 
		server ?? {server = Server.default }; 
		mater = GenOrgMutator.new(moduleSet, from);
		eater = GenOrgMutator.new(moduleSet, from);
		behavior = GenOrgBehavior.new(moduleSet, from);
		spatializer = this.getSpatialzer(type);
	}

	getSpatialzer { | type |
		case
		{type==\mono}{^MonoNucleus.new(moduleSet, from)}
		{type==\stereo}{^StereoNucleus.new(moduleSet, from)}
		{type==\quad}{^QuadNucleus.new(moduleSet, from)}
		{type==\foa}{^FOANucleus.new(moduleSet, from)}
		{type==\hoa}{^HOANucleus.new(moduleSet, from)}
		{Error("Specify spatializer type: \mono, \stereo, \quad, \foa, \hoa").throw};
	}

	buffer { ^buffer.value; }

	buffer_{ | newBuffer | 
		case
		{newBuffer.isKindOf(Buffer)}{ buffer = `newBuffer; }
		{newBuffer.isKindOf(Ref)}{ 
			if(newBuffer.value.isKindOf(Buffer), { 
				buffer = newBuffer;
			});
		};
	}

	spatializer_{ | newSpatializer | 
		if(newSpatializer.isKindOf(SpatialNucleus), { 
			spatializer = newSpatializer;
		});
	}

	behavior_{ | newBehavior |
		if(newBehavior.isKindOf(GenOrgBehavior), { 
			behavior = newBehavior;
		}); 
	} 

	play { | db = -3 | 
		spatializer.play; 
		behavior.play(
			this.buffer, 
			db, 
			spatializer.inputBus, 
			spatializer.group, 
			'addToHead'
		);
	}

	mate { | organism | 
		if(organism.isKindOf(GenOrg), { 
			var newSpatializer = spatializer.class
			.new(moduleSet, from);
			var newBehavior = behavior.mutateWith(organism.behavior);
			var newBuffer = mater.mutate(
				this.buffer, 
				organism.buffer, 
				1.0, 
			);
			^GenOrg.basicNew
			.buffer_(newBuffer)
			.behavior_(newBehavior)
			.spatializer_(newSpatializer);
		});
	}

	eat { | organism |
		if(organism.isKindOf(GenOrg), {
			this.buffer = eater.mutate(
				this.buffer, 
				organism.buffer, 
				1.0
			);
		});
	}
}	
