GOBehavior : LiveCodingEnvironment{
	classvar classInitialized = false;
	classvar <instanceCount, <instances;
	classvar currentArrayBlock, <classSymbol;
	classvar <classMatingBlock, <classEatingBlock,
	<classSearchingBlock, <classPainBlock, <classContactBlock,
	<classSpawnBlock, <classDeathBlock;

	var <matingBlock, <eatingBlock,
	<searchingBlock, <painBlock, <contactBlock,
	<spawnBlock, deathBlock;

	var <blocks, <instanceNumber;

	var isPlaying, <options;

	*new{ |matingBlock, searchingBlock,
		painBlock, contactBlock, eatingBlock,
		spawnBlock, deathBlock, options|
		var return;

		instances = instances ? List.new;

		instanceCount = instanceCount ? 0;

		this.pr_CheckGOBlocks(
			matingBlock,
			searchingBlock,
			painBlock,
			contactBlock,
			eatingBlock,
			spawnBlock,
			deathBlock
		);

		return = super.new(classSymbol:
			this.prFormatClassSymbol(this, instanceCount))
		.pr_InitGOBehavior(options);

		instances.add(return);

		if(classInitialized==false){
			ServerQuit.add({
				this.resetInstanceCount;
			});
			classInitialized = true;
		};

		^return;
	}

	pr_InitGOBehavior{|boptions|
		matingBlock = classMatingBlock;
		searchingBlock = classSearchingBlock;
		painBlock = classPainBlock;
		contactBlock = classContactBlock;
		eatingBlock = classEatingBlock;
		spawnBlock = classSpawnBlock;
		deathBlock = classDeathBlock;

		instanceNumber = instanceCount;

		options = boptions ? GOBehaviorOptions.new;
	}

	*pr_CheckGOBlocks{|m, se, pn, c, e, sp, d|

		classMatingBlock = this.checkBlock(m);
		classSearchingBlock = this.checkBlock(se);
		classPainBlock = this.checkBlock(pn);
		classContactBlock = this.checkBlock(c);
		classEatingBlock = this.checkBlock(e);
		classSpawnBlock = this.checkBlock(sp);
		classDeathBlock = this.checkBlock(d);

	}

	*checkBlock{|input|
		var return;

		if(input.class!=GOBlock){
			return = GOBlock.newRand;
		}/*ELSE*/{
			return = input;
		};

		^return;

	}

	checkBlock{|input|
		var return;

		if(input.class!=GOBlock){
			return = GOBlock.newRand;
		}/*ELSE*/{
			return = input;
		};

		^return;
	}
	/*
	*pr_ScaleGOBehavior{|...blocks|

	blocks.do{|block|

	block.ampCurve.env.levels =
	[0]++block.ampCurve.env.levels++[0];

	};

	}*/

	*defineSynthDefs{
		var symbol = this.prFormatClassSymbol(this, instanceCount);

		var types = this.types;
		var roles = this.roles;

		[
			classMatingBlock, classSearchingBlock,
			classPainBlock, classContactBlock,
			classEatingBlock, classSpawnBlock,
			classDeathBlock
		].do{|block, i|

			var dictionary = Dictionary.new;

			var wrappers = block.do{|curve, x|

				var type = types[i].asString;
				var role = roles[x].asString, name;

				dictionary[role.asSymbol] = { |timescale = 1|
					SynthDef.wrap({
						var lo = format("%Lo", role).asSymbol.kr(0);
						var hi = format("%Hi", role).asSymbol.kr(0);

						EnvGen.ar(curve.env,
							timeScale: timescale,
							doneAction: Done.none
						).range(lo, hi);
					});

				};
			};

			var type = this.types[i];
			var synthdefname =  type.asString;
			var synthdef = SynthDef(type.asSymbol, {
				var buf = \buf.kr(0);
				var timescale = \timescale.kr(1);

				var rate = dictionary[\rate].value(timescale);
				var pos = dictionary[\pos].value(timescale);
				var amp = dictionary[\amp].value(timescale);
				var ffreq = dictionary[\ffreq].value(timescale);
				var impulseRate = dictionary[\impulseRate].value(timescale);
				var grainDur = dictionary[\grainDur].value(timescale);
				var rq = dictionary[\rq].value(timescale);

				var bufdur = BufDur.kr(buf);

				var phasor = pos * bufdur;

				var impulse = Impulse.ar(impulseRate);

				var sig = TGrains.ar(
					1,
					impulse,
					buf,
					rate,
					phasor,
					grainDur
				) * 16;

				var filteredSig = BPF.ar(
					sig,
					ffreq,
					rq
				);

				var out = filteredSig * amp
				* EnvGen.ar(
					Env([0, 1, 1, 1, 0], [0.05, 1, 1, 0.05].normalizeSum,
						curve: \welch), timeScale: timescale,
					doneAction: Done.freeSelf
				) * \ampDB.kr(-12).dbamp;
				Out.ar(\out.kr(0), out);

			});

			this.registerSynthDef(synthdef, false, symbol);
		};

		instanceCount = instanceCount + 1;
	}

	*prSetClassSymbol{

		classSymbol = super.prFormatClassSymbol(this, true, instanceCount);
		^classSymbol;

	}

	getInstanceNumberSymbol{
		var symb = this.prFormatClassSymbol(this);
		symb = format("%%", symb.asString, instanceNumber).asSymbol;
		^symb;
	}

	pr_GOBehaviorCheckType{|type|
		var bool = false;
		var types = this.types;
		var size = types.size;

		var stringToThrow;

		block{|break|
			types.do{|item|
				if(type===item){
					bool = true;
					break.value(999);
				}
			}
		};

		if(bool==false){
			stringToThrow = "Choose organism action from the following type: ";

			types.do{|item, index|
				stringToThrow = stringToThrow++"\n\t"++item;
				if(index <= size - 2){
					stringToThrow = stringToThrow++",";
				}
			};

			Error(stringToThrow).throw;
		};
	}

	playGOBehavior{|type, buffer, db = -12, outBus = 0, target, action|

		var localSymb = this.localClassSymbol;

		var synthName = format("%_%", localSymb.asString, type.asString).asSymbol;

		var timescale = options.timescale * rrand(0.95, 1.05);

		type = type !? {
			type.asString.toLower.asSymbol;
		} ? 'contacting';

		this.pr_GOBehaviorCheckType(type);

		if(target.isNil){
			target = server.defaultGroup;
		};

		if(action.isNil){
			if(target==server.defaultGroup){
				action = 'addToHead';
			}/*ELSE*/{
				action = 'addBefore';
			};
		};

		Synth(synthName, [
			\buf, buffer,

			\rateLo, options.rateLo * rrand(0.95, 1.05),
			\rateHi, options.rateHi * rrand(0.95, 1.05),

			\posLo, options.posLo,
			\posHi, options.posHi,

			\ampLo, options.ampLo,
			\ampHi, options.ampHi,

			\ffreqLo, options.ffreqLo,
			\ffreqHi, options.ffreqHi,

			\impulseRateLo, options.impulseRateLo,
			\impulseRateHi, options.impulseRateHi,

			\grainDurLo, options.grainDurLo,
			\grainDurHi, options.grainDurHi,

			\rqLo, options.rqLo,
			\rqHi, options.rqHi,

			\timescale, timescale * options.timescaleScalar,

			\ampDB, db,
			\out, outBus

		], target, action);

	}

	*types{
		var return = #[
			'mating',
			'searching',
			'pain',
			'contacting',
			'eating',
			'spawn',
			'death'
		];
		^return;
	}

	types{
		^this.class.types;
	}

	*roles{
		var return = #[
			'rate',
			'pos',
			'amp',
			'ffreq',
			'impulseRate',
			'grainDur',
			'rq'
		];

		^return
	}

	roles{
		^this.class.roles;
	}


	localClassSymbol{
		var return = this.class.superclass
		.prFormatClassSymbol(this, instanceNumber);
		^return;
	}

	free{
		var localSymb = this.localClassSymbol;

		super.free;
		instances.remove(this);


		synthDefDictionary !? {

			synthDefDictionary[localSymb] !? {
				synthDefDictionary[localSymb]
				.keys.do{|synthDefName|
					SynthDef.removeAt(synthDefName);
				};

				synthDefDictionary.removeAt(localSymb);
			};

		}
	}

	*freeAll{
		if(instances.isNil.not){
			instances.copy.do{|item|

				item.free;

			};

		};

		this.resetInstanceCount;
	}

	*resetInstanceCount{
		instanceCount = 0;
	}
}