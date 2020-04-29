GenOrg_EatingMutator : GenOrg_Mutator{

	*new{|synthDef|
		^super.new(synthDef);
	}

	synhtDef_{|newSynthDef|

		if(newSynthDef.isNil){
			newSynthDef = this.pr_DefaultSynthDef;
		};

		newSynthDef.name = this.pr_DefaultSynthDefName;

		super.synthDef_(newSynthDef);

	}


	pr_DefaultSynthDefName{
		^(\GenOrg_Mutator_EatingSynth);
	}

	pr_GetSynthMsg{|buffer0, buffer1, timescale = 1|
		^(Synth.basicNew(synthDef,
			server)).newMsg(args: [
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
		]);
	}

	*pr_DefaultSynthDef{
		^SynthDef.new(this.pr_DefaultSynthDefName, {
			var timescale = \timescale.kr(1);
			var buf0 = \buf0.kr(0);
			var buf1 = \buf1.kr(1);

			var sig0 = PlayBuf.ar(
				1,
				buf0,
				BufRateScale.kr(buf0) * \rate0.kr(1.0)
			);

			var sig1 = PlayBuf.ar(
				1,
				buf1,
				BufRateScale.kr(buf1) * \rate0.kr(1.0)
			);

			var fftsize = 2.pow(9);
			var chain0 = FFT(LocalBuf(fftsize), sig0);
			var chain1 = FFT(LocalBuf(fftsize), sig1);
			var morphRateRate = \morphRateRate.kr(0.125);
			var morphRateHi = \morphRateHi.kr(10.5);
			var wipeMax = \wipeMax.kr(0.1);

			var chain = PV_Morph(
				chain0,
				chain1,
				LFNoise2.ar(

					LFNoise1.kr(morphRateRate)
					.range(
						morphRateRate * 2,
						morphRateRate * 2 + morphRateHi
					)

				).unipolar(wipeMax);
			);

			var out = IFFT(chain) * \ampDB.kr(-3).dbamp;

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
