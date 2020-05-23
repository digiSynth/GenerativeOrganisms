+ SpatialNucleus {

	*newHOA{
		^SpatialCellHOA.new;
	}

	*newFOA{
		^SpatialCellFOA.new;
	}

	*newBinaural{
		^SpatialCellBinaural.new;
	}

	*formatSynthName{|name|
		var return;
		var symbol = this.prFormatClassSymbol(this);
		return = super.formatSynthName(name, symbol);

		^return;
	}

	formatSynthName{|name|
		var return = this.class.formatSynthName(name);
		^return;
	}

}