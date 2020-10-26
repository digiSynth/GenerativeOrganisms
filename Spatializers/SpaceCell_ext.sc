+ SpaceCell {

	*newHOA{
		^SpaceCellHOA.new;
	}

	*newFOA{
		^SpaceCellFOA.new;
	}

	*newBinaural{
		^SpaceCellBinaural.new;
	}

	// *isSpaceCell{^true}
	isSpaceCell{^true}

	*isSpaceCell{^true}

	//overriding methods: these are part of the interface associated with the synthDefManagementSystem superclass
	//it's sort of bulky but like whatever
	*getControlNames{ |synthDefName = \sine|
		var symbol = this.prFormatClassSymbol(this);
		var formattedName = this.formatSynthName(synthDefName);
		var return = super.getControlNames(synthDefName, symbol);
		^return;

	}

	getControlNames{|synthDefName = \sine|
		var formattedName = this.formatSynthName(synthDefName);
		var return = this.class.getControlNames(formattedName);
		^return;
	}

	*postControlNames{|synthDefName = \sine|
		var symbol = this.prFormatClassSymbol(this);
		var formattedName = this.formatSynthName(synthDefName);
		super.postControlNames(formattedName, symbol);
	}

	postControlNames{|synthDefName = \sine|
		this.class.postControlNames(synthDefName);
	}

	*postSynthsAndControls{
		var symbol = this.prFormatClassSymbol(this);
		super.postSynthsAndControls(symbol);
	}

	postSynthsAndControls{
		this.class.postSynthsAndControls;
	}

	/*	*prSetClassSymbol{
	classSymbol = this.prFormatClassSymbol(this);
	}*/

	/*	*registerSynthDef{
	|synthdef, addIt = false|
	super.registerSynthDef(synthdef, addIt, classSymbol);
	}

	registerSynthDef{|synthdef, addIt=false|
	this.class.registerSynthDef(synthdef, addIt);
	}*/

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

+Object{
	isSpaceCell{^false}

	*isSpaceCell{^false}
}
