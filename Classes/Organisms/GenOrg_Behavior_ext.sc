+ GenOrg_Behavior{


	getControlNames{|synthDefName = \sine|

		var formattedName = this.formatSynthName(synthDefName);
		var return = this.class.getControlNames(formattedName, this.localClassSymbol);
		^return;

	}

	postControlNames{|synthDefName = \sine|

		this.class.postControlNames(synthDefName, this.localClassSymbol);

	}

	postSynthsAndControls{

		this.class.postSynthsAndControls(this.localClassSymbol);

	}

	registerSynthDef{|synthdef, addIt=false|

		this.class.registerSynthDef(synthdef, addIt, this.localClassSymbol);

	}


	formatSynthName{|name|

		var return = this.class.formatSynthName(name, this.localClassSymbol);

		^return;
	}
}