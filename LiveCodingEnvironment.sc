LiveCodingEnvironment{
	classvar <liveCodingInstances;
	classvar <synthDefGarbage, synthDefGarbageCollectorRoutine;
	classvar synthDefQueue, synthDefQueueLoader;
	classvar <synthDefDictionary, server;
	classvar quitNotified = false, treeNotified = false;
	classvar synthDef_ON_Loader, synthDef_OFF_Loader;

	*new{|classSymbol|
		var return;

		server = Server.default;
		this.pr_Initialize(classSymbol);

		return = super.newCopyArgs;

		liveCodingInstances = liveCodingInstances ? List.new;
		liveCodingInstances.add(return);

		^return;
	}

	*pr_RemoveInstance{|toRemove|
		liveCodingInstances.remove(toRemove);
	}

	*pr_Initialize{|classSymbol|
		var shouldWeLoadSynthDefs = false;

		if(server.hasBooted){
			synthDefDictionary = synthDefDictionary ? Dictionary.new;
			// synthDefLoader = synthDefLoader ? SynthDefLoader.new;
			classSymbol ?? {
				Error("No class symbol supplied!").throw;
			};

			synthDefDictionary[classSymbol] =
			synthDefDictionary[classSymbol] ?? {
				shouldWeLoadSynthDefs = true;
				Dictionary.new;
			};

			this.pr_InitAddSynthDefs(shouldWeLoadSynthDefs, classSymbol);

			if(quitNotified==false){

				ServerQuit.add({
					this.freeSynthDefDictionary;
				});

				quitNotified = true;

			};
		}/*ELSE*/{

			Error("Server is not booted!").throw;

		};
	}

	*prSetClassSymbol{

		this.subclassResponsibility(thisMethod);

	}

	*prFormatClassSymbol{|object, id|
		var return;
		var splitArray = object.class.asString.split($_);
		if(splitArray[0]=="Meta" and: {splitArray.size==2}){
			return = splitArray[1];
		}/*ELSE*/{
			splitArray[1..(splitArray.size - 1)].do{ |item, index|
				return = return++item;
				if(index < (splitArray.size - 2)){
					return = return++"_";
				};
			};
		};
		if(id.isNil.not){
			return = return++(id.asString);
		};
		return = return.asSymbol;
		^return;
	}

	*pr_InitAddSynthDefs{|goForIt = false, classSymbol|
		if(goForIt){
			this.pr_AddSynthDefs(classSymbol);
		};
	}

	*pr_AddSynthDefs{|inputClassSymbol|

		this.defineSynthDefs;
		synthDef_ON_Loader = synthDef_ON_Loader ? SynthDef_OnLoader.new(server);
		synthDef_ON_Loader.load(synthDefDictionary[inputClassSymbol].asArray);

	}

	*registerSynthDef{|synthDef, addIt = false, inputClassSymbol|
		var formattedName = this.formatSynthName(synthDef.name, inputClassSymbol);
		synthDef.name = formattedName;

		//load the synthdef into the correct synthDef dictionary
		if(synthDefDictionary[inputClassSymbol].isNil){
			synthDefDictionary[inputClassSymbol] = Dictionary.new;
		};

		synthDefDictionary[inputClassSymbol].add(synthDef.name -> synthDef);

		//if instructed
		if(addIt){
			//add that synthdef to the server immediately
			synthDefDictionary[inputClassSymbol][synthDef.name].add;

		};

	}

/*	*getControlNames{|synthDefName = \sine, inputClassSymbol|

		var targetList = synthDefDictionary[inputClassSymbol];
		var nameArray = [];

		if(targetList.isNil.not){
			var listItem = targetList[synthDefName];
			var ic = listItem.class;
			var controlNameArray;

			switch(ic)
			{SynthDef}{controlNameArray = listItem.allControlNames}
			{Dictionary}{
				block{ |break|
					listItem.do{|subItem|
						if(subItem.class==SynthDef){
							controlNameArray = subItem.allControlNames;
							break.value(999);
						};
					};
				};
			};

			nameArray = controlNameArray
			.asArray.collect({|control| control.name});
		};

		^nameArray;
	}*/

	*formatSynthName{|name, inputClassSymbol|
		var return = name;

		if(name.asString.contains(inputClassSymbol.asString)==false){
			return = format(
				"%_%",
				inputClassSymbol.asString,
				name.asString
			).asSymbol;

		};

		^return;
	}

	*defineSynthDefs{

		this.subclassResponsibility(thisMethod);
		/*
		This establishes the class as an abstract type.
		In C++ one would see this method written as something similar to:

		virtual void defineSynthDefs(){} = 0;
		*/

		//Use this method in subclasses to register synthdefs.
	}

	free{
		liveCodingInstances.remove(this);
	}

	*freeAll{
		liveCodingInstances.copy.do{|item|
			item.free;
		};
	}

/*	*freeAllDeep{

		this.freeAll;
		this.freeSynthDefDictionary;

	}*/

	*freeSynthDefDictionary{
		var arrayOfSynthDefs;

		synthDefDictionary.copy.do{|dictionary|
			dictionary.do{|synthDef|
				arrayOfSynthDefs = arrayOfSynthDefs.add(synthDef);
			};
		};

		this.pr_GarbageCollect(arrayOfSynthDefs);

		synthDefDictionary = Dictionary.new;
	}

	*pr_RemoveSingleSynthDef{

		|inputClassSymbol, synthDefName|

		if(inputClassSymbol.class!=Symbol){
			Error("Can only remove key from dictionary that is a symbol").throw;
		};

		if(synthDefName.class==SynthDef){
			synthDefName = synthDefName.name;
		};

		this.pr_GarbageCollect(
			synthDefDictionary[inputClassSymbol].removeAt(synthDefName)
		);
	}

	*removeSynthDefs{|dictionaryKey|

		var dictionary;

		if(dictionaryKey.class!=Symbol){
			Error("Can only remove key from dictionary that is a symbol").throw;
		};
		dictionary = synthDefDictionary.removeAt(dictionaryKey);
		if(dictionary.isNil.not){
			this.pr_GarbageCollect(dictionary.asArray);
			dictionary = nil;
		};

	}

	*pr_GarbageCollect{|synthDef|

		fork{
			synthDef_OFF_Loader = SynthDef_OffLoader.new(server);

			if(synthDef_ON_Loader.isNil.not){
				if(synthDef_ON_Loader.isRunning){
					while({synthDef_ON_Loader.isRunning}, {1e-3.wait});
				};
			};

			synthDef_OFF_Loader.offLoad(synthDef);

		};

	}

	*instances{

		^liveCodingInstances;

	}

}