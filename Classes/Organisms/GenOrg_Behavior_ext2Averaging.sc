+ GenOrg_Behavior{
	averageBehaviors{|behavior|
		var check = {

			if(behavior.class != GenOrg_Behavior){
				Error("Can only average GenOrg_Behaviors!").throw;
			};

		}.value;

		var averageBehavior;

		var newMatingBlock = matingBlock.averageBlocks(behavior.matingBlock);
		var newEatingBlock = eatingBlock.averageBlocks(behavior.eatingBlock);
		var newSearchingBlock = searchingBlock.averageBlocks(behavior.searchingBlock);
		var newPainBlock = painBlock.averageBlocks(behavior.painBlock);
		var newContactBlock = contactBlock.averageBlocks(behavior.contactBlock);

		var newOptions = options.averageOptions(behavior.options);

		averageBehavior = GenOrg_Behavior.new(
			matingBlock: newMatingBlock,
			searchingBlock: newSearchingBlock,
			painBlock: newPainBlock,
			contactBlock: newContactBlock,
			eatingBlock: newEatingBlock,
			options: newOptions
		);

		^averageBehavior;

	}
}