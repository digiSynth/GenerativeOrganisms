+ GenOrg_Behavior{

	averageBehaviors{|behavior|

		var newBlock = genOrg_Block.averageBlocks(behavior.genOrg_Block);
		var newOptions = options.averageOptions(behavior.options);
		^GenOrg_Behavior(newBlock, newOptions);

	}


	registerSynthDef{|synthdef, addIt=false|

		this.class.registerSynthDef(synthdef, addIt, this.localClassSymbol);

	}


	formatSynthName{|name|

		var return = this.class.formatSynthName(name, this.localClassSymbol);

		^return;
	}
}