//This class is too slow. I should probably just rewrite it. Or really remove the different synths. Just one at a time.
GenOrg_Behavior : SynthDef_Processor{
	classvar classInitialized = false;
	classvar <instanceCount, <goBehaviorInstances;
	classvar currentArrayBlock, <classSymbol;
	classvar <classGenOrg_Block;

	var <genOrg_Block, <instanceNumber;
	var isPlaying, <options;

	*new{ |block, options|
		var return, copyCount;

		goBehaviorInstances = goBehaviorInstances ? List.new;

		instanceCount = instanceCount ? 0;
		copyCount = instanceCount;

		classGenOrg_Block = this.pr_CheckBlock(block);

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
		genOrg_Block = classGenOrg_Block;
		instanceNumber = num;
		options = boptions ? GenOrg_BehaviorOptions.new;
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

	*defineSynthDefs{
		var symbol = this.prFormatClassSymbol(this, instanceCount);
		var arguments = this.arguments;
		var dictionary = Dictionary.new;
		var synthdefname, synthdef;

		arguments.do({|key, index|
			dictionary[key] = { |timescale = 1|
				SynthDef.wrap({
					var lo = format("%Lo", key).asSymbol.kr(0);
					var hi = format("%Hi", key).asSymbol.kr(1);

					EnvGen.ar(classGenOrg_Block[index].env,
						timeScale: timescale,
						doneAction: Done.none
					).range(lo, hi);
				});
			};
		});

		synthdef = SynthDef(\Synth, {
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

	playBehavior{|buffer, db = -12, outBus = 0, target, action|

		var localSymb = this.localClassSymbol;
		var timescale = options.timescale * rrand(0.95, 1.05);

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

		Synth(this.formatSynthName(\Synth, this.localClassSymbol), [
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

	*arguments{
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

	arguments{
		^this.class.arguments;
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

	*resetInstanceCount{
		instanceCount = nil;
	}
}
