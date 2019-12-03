LiveCodingEnvironment{
	classvar <instances;
	// classvar <synthDefLoader;
	classvar synthDefQueue, synthDefQueueLoader;
	classvar <synthDefDictionary, server;
	classvar quitNotified = false;

	*new{|classSymbol|
		var return;

		server = Server.default;
		this.pr_Initialize(classSymbol);

		return = super.newCopyArgs;

		instances = instances ? List.new;
		instances.add(return);

		^return;
	}

	*pr_RemoveInstance{|toRemove|
		instances.remove(toRemove);
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

					var synthDefDictionaryCopy = synthDefDictionary.copy;

					ServerBoot.add({
						synthDefDictionaryCopy.do{|dictionary|

							dictionary.copy.do{|synthDef|
								SynthDef.removeAt(synthDef.name);
							};

						};
					});

					synthDefDictionary = nil;
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
			this.pr_MakeSynthDefs(classSymbol);
		};

	}

	*pr_MakeSynthDefs{|inputClassSymbol|

		if(synthDefQueueLoader.isNil){

			synthDefQueueLoader = Task({

				this.pr_DefineSynthDefs(inputClassSymbol);

				synthDefQueue.value.copy.size.do{|index|

					synthDefQueue.value.removeAt(0).send(server);

				};

				server.sync;

			}).play;

		}/*ELSE*/{

			this.pr_DefineSynthDefs(inputClassSymbol);
			synthDefQueueLoader.reset;

		};

		if(synthDefQueueLoader.isPlaying.not){

			synthDefQueueLoader.play;

		};

	}


	*pr_DefineSynthDefs{ |inputClassSymbol|

		this.defineSynthDefs;
		this.pr_AddToSynthDefQueue(synthDefDictionary[inputClassSymbol]);

	}

	*pr_AddToSynthDefQueue{|input|
		synthDefQueue = synthDefQueue ? [];

		if(input.isCollection){
			input.do{|synthDef|
				synthDefQueue = synthDefQueue.add(synthDef);
			};
		}/*ELSE*/{

			if(input.class==SynthDef){
				synthDefQueue = synthDefQueue.add(input);
			};

		};

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

		}/*ELSE*/{
			this.pr_AddToSynthDefQueue(synthDefDictionary[inputClassSymbol][synthDef.name]);
		}

	}

	*getControlNames{|synthDefName = \sine, inputClassSymbol|

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
	}

	*postControlNames{|synthDefName, inputClassSymbol|
		var controlNameArray = this.getControlNames(synthDefName, inputClassSymbol);
		format("SynthDef name: %", synthDefName).postln;
		"Control names: [\n".post;
		controlNameArray.do{|item, index|
			"\t\t".post; item.post;
			if(index < (controlNameArray.size - 1)){
				",".post;
			};
			"\n".post;
		};
		"];\n".postln;
	}

	*postSynthsAndControls{|inputClassSymbol|
		var targetList = synthDefDictionary[inputClassSymbol];

		if(targetList.isNil.not){

			targetList.do{|synthDef|
				this.postControlNames(synthDef.name, inputClassSymbol);
			};

		};
	}

	*postAllSynthsAndControls{
		synthDefDictionary.keys.do{|key, index|

			"------------------------------".postln;
			("Class: "++key++"\n").postln;
			this.postSynthsAndControls(key);
			"------------------------------".postln;

		};
	}

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
		instances.remove(this);
	}

	*freeAll{
		instances.copy.do{|item|
			item.free;
		};
	}


	*freeAllDeep{
		this.freeAll;
		this.freeSynthDefDictionary;

	}

	*freeSynthDefDictionary{
		synthDefDictionary.copy.do{|dictionary|

			dictionary.copy.do{|synthDef|
				SynthDef.removeAt(synthDef.name);
			};

		};

		synthDefDictionary = Dictionary.new;
	}

}