//This class is too slow. I should probably just rewrite it. Or really remove the different synths. Just one at a time.
GenOrg_Behavior : SynthDef_Processor{
	classvar classInitialized = false;
	classvar <instanceCount, <goBehaviorInstances;
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
		var return, copyCount;

		goBehaviorInstances = goBehaviorInstances ? List.new;

		instanceCount = instanceCount ? 0;
		copyCount = instanceCount;

/*		matingBlock = matingBlock ? GenOrg_Block.newRand;
		searchingBlock = searchingBlock ? GenOrg_Block.newRand;
		painBlock = painBlock ? GenOrg_Block.newRand;
		contactBlock = contactBlock ? GenOrg_Block.newRand;
		eatingBlock = eatingBlock ? GenOrg_Block.newRand;
		spawnBlock = spawnBlock ? GenOrg_Block.newRand;
		deathBlock = deathBlock ? GenOrg_Block.newRand;*/
		this.pr_CheckGenOrg_Blocks(
			matingBlock,
			searchingBlock,
			painBlock,
			contactBlock,
			eatingBlock,
			spawnBlock,
			deathBlock
		);

		return = super.new(classSymbol:
			this.prFormatClassSymbol(this, instanceCount)
		)
		.pr_InitGenOrg_Behavior(options, copyCount);

		goBehaviorInstances.add(return);

		if(classInitialized==false){
			ServerQuit.add({
				this.resetInstanceCount;
			});

			ServerTree.add({
				if(goBehaviorInstances.isEmpty.not){
					goBehaviorInstances.copy.do{|instance|
						goBehaviorInstances.removeAt(0).free;
					};
				};
			});

			classInitialized = true;
		};

		^return;
	}

	pr_InitGenOrg_Behavior{|boptions, num|

		matingBlock = classMatingBlock;
		searchingBlock = classSearchingBlock;
		painBlock = classPainBlock;
		contactBlock = classContactBlock;
		eatingBlock = classEatingBlock;
		spawnBlock = classSpawnBlock;
		deathBlock = classDeathBlock;

		instanceNumber = num;

		options = boptions ? GenOrg_BehaviorOptions.new;
	}

	*pr_CheckGenOrg_Blocks{|m, se, pn, c, e, sp, d|

		classMatingBlock = this.pr_CheckBlock(m);
		classSearchingBlock = this.pr_CheckBlock(se);
		classPainBlock = this.pr_CheckBlock(pn);
		classContactBlock = this.pr_CheckBlock(c);
		classEatingBlock = this.pr_CheckBlock(e);
		classSpawnBlock = this.pr_CheckBlock(sp);
		classDeathBlock = this.pr_CheckBlock(d);

	}

	*pr_CheckBlock{|input|
		var return;

		if(input.class!=GenOrg_Block){
			return = GenOrg_Block.newRand;
		}/*ELSE*/{
			return = input;
		};

		^return;

	}

	checkBlock{|input|
		var return;

		if(input.class!=GenOrg_Block){
			return = GenOrg_Block.newRand;
		}/*ELSE*/{
			return = input;
		};

		^return;
	}

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

				var out = Normalizer.ar(filteredSig) * amp
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

	pr_GenOrg_BehaviorCheckType{|type|
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

	playGenOrg_Behavior{|type, buffer, db = -12, outBus = 0, target, action|

		var localSymb = this.localClassSymbol;
		var synthName = format("%_%", localSymb.asString, type.asString).asSymbol;
		var timescale = options.timescale * rrand(0.95, 1.05);

		type = type !? {
			type.asString.toLower.asSymbol;
		} ? 'contacting';

		this.pr_GenOrg_BehaviorCheckType(type);

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
		var synthDefs = synthDefDictionary[localSymb].asArray;

		this.class.pr_GarbageCollect(synthDefs);
		super.free;
		goBehaviorInstances.remove(this);
	}

/*	*freeAll{
		if(goBehaviorInstances.isNil.not){
			goBehaviorInstances.copy.do{|item|
				item.free;
			};

			instanceCount = 0;
			goBehaviorInstances = nil;
		};
	}*/

	*resetInstanceCount{
		instanceCount = nil;
	}
}