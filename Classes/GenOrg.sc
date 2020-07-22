GenOrg {    
	var moduleSet, from, buffer, freeFunc; 
	var server, <mater, <eater, <cell, <membrane; 

	*new { | membraneType(\mono), moduleSet(\default), from | 
		^super.newCopyArgs(moduleSet, from).initGenOrg(membraneType);
	}

	modulSet_{ | newModuleSet, newFrom | 
		membrane.moduleSet_(newModuleSet, newFrom); 
		cell.moduleSet_(newModuleSet, newFrom); 
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
		cell = GenOrgCell.new(moduleSet, from);
		membrane = this.getSpatialzer(type);
	}

	getSpatialzer { | type |
		case
		{type==\mono}{^MonoMembrane.new(moduleSet, from)}
		{type==\stereo}{^StereoMembrane.new(moduleSet, from)}
		{type==\quad}{^QuadMembrane.new(moduleSet, from)}
		{type==\foa}{^FOAMembrane.new(moduleSet, from)}
		{type==\hoa}{^HOAMembrane.new(moduleSet, from)}
		{Error("Specify membrane type: \mono, \stereo, \quad, \foa, \hoa").throw};
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

	membrane_{ | newSpatializer | 
		if(newSpatializer.isKindOf(SpatialMembrane), { 
			membrane = newSpatializer;
		});
	}

	cell_{ | newCell |
		if(newCell.isKindOf(GenOrgCell), { 
			cell = newCell;
		}); 
	} 

	play { | db = -3 | 
		membrane.play; 
		cell.play(
			this.buffer, 
			db, 
			membrane.inputBus, 
			membrane.group, 
			'addToHead'
		);
	}

	mate { | organism | 
		if(organism.isKindOf(GenOrg), { 
			var newSpatializer = membrane.class
			.new(moduleSet, from);
			var newCell = cell.mutateWith(organism.cell);
			var newBuffer = mater.mutate(
				this.buffer, 
				organism.buffer, 
				1.0, 
			);
			^GenOrg.basicNew
			.buffer_(newBuffer)
			.cell_(newCell)
			.membrane_(newSpatializer);
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
