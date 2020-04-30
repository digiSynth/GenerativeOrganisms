GenOrg_MatingMutator : GenOrg_Mutator{

	*new{|synthDef|
		^super.new.synthDef_(synthDef);
	}

	pr_DefaultSynthDefName{
		^(\GenOrg_Mutator_MatingSynth);
	}

	pr_GetSynthMsg{|buffer0, buffer1, timescale = 1|
		^(
			synthMsg: (Synth.basicNew(synthDef.name)
			).newMsg(args: [
				\timescale, timescale,
				\buf0, buffer0,
				\buf1, buffer1,

				\rate0, 1,
				\rate1, rrand(0.9, 1.1),

				\morphRateRate, timescale * exprand(1.05, 6.0),
				\morphRateHi, exprand(1.05, 6.0),

				\choiceRateRate, timescale * exprand(1.5, 3.0),
				\choiceRateHi, exprand(2.0, 6.0),

				\ampDB, exprand(1.0, 4.0) - 1,

				\wipeMax, exprand(0.1, 0.9)
			]),

			synthDef: synthDef
		);
	}

	pr_DefaultSynthDef{
		^SynthDef.new(this.pr_DefaultSynthDefName, {
			var timescale = \timescale.kr(1);
			var numSegs = exprand(2, 24).round(2).asInteger;
			var impulserate = \impulserate.kr(40);
			var impulse = Impulse.kr(impulserate);

			var buf0 = \buf0.kr(0);
			var bufdur0 = BufDur.kr(buf0);

			var buf1 = \buf1.kr(1);
			var bufdur1 = BufDur.kr(buf1);

			var durMul = \durMul.kr(4);

			var rate0RateRate = \rate0RateRate.kr(0.1);
			var sig0 = TGrains.ar(
				1,
				impulse,
				buf0,

				\rate0.kr(1),

				// Line.ar(0, bufdur0, timescale),

				EnvGen.ar(Env(
					({Rand(0.0, bufdur0)}!numSegs),
					({ExpRand(0.1, 2.0)}!(numSegs-1)).normalizeSum,
					({Rand(-12, 12)}!(numSegs - 1))
				),
				timeScale: timescale
				),
				durMul/impulserate
			);

			var rate1RateRate = \rate1RateRate.kr(0.1);
			var sig1 = TGrains.ar(
				1,
				impulse,
				buf1,
				\rate1.kr(1),

				// Line.ar(0, bufdur1, timescale),
				EnvGen.ar(Env(
					({Rand(0.0, bufdur1)}!numSegs),
					({ExpRand(0.1, 2.0)}!(numSegs-1)).normalizeSum,
					({Rand(-12, 12)}!(numSegs - 1))
				),

				timeScale: timescale,
				doneAction: Done.none),
				durMul/impulserate
			);

			var fftsize = 2.pow(9);
			var chain0 = FFT(LocalBuf(fftsize), sig0);
			var chain1 = FFT(LocalBuf(fftsize), sig1);

			var morphRateRate = \morphRateRate.kr(0.125);

			var randSig = LFNoise2.ar(LFNoise1.kr(morphRateRate)
				.range(morphRateRate * 2,
					morphRateRate * 2 * \morphRateHi.kr(10.5))
			).unipolar(1);

			var chain = PV_Morph(chain0, chain1,
				LFNoise2.ar(LFNoise1.kr(morphRateRate)
					.range(morphRateRate * 2,
						morphRateRate * 2
						+ \morphRateHi.kr(10.5))).unipolar(1);
			);


			var arrayToChoose = [IFFT(chain), sig0, sig1];

			var choiceRateRate = \choiceRateRate.kr(0.5);

			var choiceRate = LFNoise1.kr(choiceRateRate).exprange(
				choiceRateRate * 2 + \choiceRateLo.kr(0.1),
				choiceRateRate * 2 + \choiceRateHi.kr(2.0)
			);

			var which = LFNoise1.kr(choiceRate).unipolar(3.0).floor;

			var out = SelectXFocus.ar(which, arrayToChoose,
				LFNoise1.kr(timescale * Rand(1.0, 4.0)).unipolar,
				true
			);

			out = LPF.ar(out, 16000);
			out = HPF.ar(out, 20);

			out = out * EnvGen.ar(
				Env([0, 1, 1, 1, 0], [0.1, 1, 1, 0.1]
					.normalizeSum, \lin),
				timeScale: timescale,
				doneAction: Done.freeSelf
			);

			out = Normalizer.ar(out, 0.9);

			Out.ar(\out.kr(0), out * \ampDB.kr(-3).dbamp);

		});
	}

}