GenerativeMutator{
	classvar isInitialized = false;
	classvar server;
	classvar condition;

	*pr_GOMutatorInit{

		if(isInitialized==false){
			server = Server.default;

			if(server.hasBooted==false){
				Error("Server has not been booted!").throw;
			};

			this.pr_CheckDirectory;

			isInitialized = true;
		};

	}

	*mateBehaviors{|behavior1, behavior2|
		var matedBehavior = behavior1.averageBehaviors(behavior2);
		^matedBehavior;
	}

	*pr_CheckDirectory{
		if(File.exists(PathName.tmp +/+ "SC_GOrganisms")==false){
			File.mkdir(PathName.tmp +/+ "SC_GOrganisms");
		};
	}

	*mateBuffers{|buffer0, buffer1, action|

		this.pr_GOMutatorInit;

		if(isInitialized){
			var return = Ref.new(nil);

			var synth;
			var timescale = 1;
			var score = Score.new;

			var buffer0Copy = Buffer.new(server, server.sampleRate, 1);
			var buffer1Copy = Buffer.new(server, server.sampleRate, 1);

			var oscpath = PathName.tmp +/+ UniqueID.next ++".osc";
			var outpath = PathName.tmp +/+ "SC_GOrganisms"
			+/+ UniqueID.next ++ ".wav";

			score.add([
				0, buffer0Copy.allocMsg,
				buffer0Copy.readMsg(buffer0.path);
			]);

			score.add([
				0, buffer1Copy.allocMsg,
				buffer1Copy.readMsg(buffer1.path);
			]);

			score.add([
				0, [\d_recv, SynthDef.new(\GOMutator_MatingSynth, {
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

					var out = Select.ar(which, arrayToChoose) * \ampDB.kr(-3).dbamp;
					out = LeakDC.ar(out);
					out = LPF.ar(out, 16000);
					out = HPF.ar(out, 20);
					Out.ar(\out.kr(0), out * EnvGen.ar(
						Env([0, 1, 1, 1, 0], [0.025, 1, 1, 0.025]
							.normalizeSum, \lin),
						timeScale: timescale,
						doneAction: Done.freeSelf
					));
				}).asBytes]
			]);

			score.add([
				0, (synth = Synth.basicNew(\GOMutator_MatingSynth,
					server)).newMsg(args: [
					\timescale, timescale,
					\buf0, buffer0Copy,
					\buf1, buffer1Copy,

					\impulserate, exprand(32.0, 90.0),
					\durMul, exprand(4.0, 16.0),

					\rate0, rrand(0.75, 1.5),
					\rate1, rrand(0.75, 1.5),

					\morphRateRate, timescale * exprand(1.0, 6.0),
					\morphRateHi, exprand(1.05, 2.0),

					\choiceRateRate, timescale * exprand(1.5, 2.0),
					\choiceRateHi, exprand(1.0, 3.0),

					\ampDB, 18
				]);
			]);

			score.add([timescale, 0]);

			score.sort;

			score.recordNRT(
				oscpath,
				outpath,
				nil,
				48e3,
				"wav",
				"int24",
				ServerOptions.new
				.numInputBusChannels_(0)
				.numOutputBusChannels_(1)
				.sampleRate_(48e3)
				.memSize_(2.pow(19))
				.numWireBufs_(2.pow(13))
				.verbosity_(-2),
				action: {
					fork{
						var localCondition = Condition.new;
						var toReturn;

						buffer0Copy.freeMsg;
						buffer1Copy.freeMsg;

						File.delete(oscpath);

						toReturn = Buffer.read(server, outpath, action: {
							localCondition.unhang;
						});

						localCondition.hang;
						return.value = toReturn;

						action.value;

					};

				}

			);

			^return;
		}/*ELSE*/{
			this.pr_GOMutatorInit;
			this.mateBuffers(buffer0, buffer1, action);
		};
	}

	*eatBuffers{|buffer0, buffer1, action|

		this.pr_GOMutatorInit;

		if(isInitialized){
			var return = Ref.new(nil);

			var synth;
			var timescale = 1;
			var score = Score.new;

			var buffer0Copy = Buffer.new(server, server.sampleRate, 1);
			var buffer1Copy = Buffer.new(server, server.sampleRate, 1);

			var oscpath = PathName.tmp +/+ UniqueID.next ++".osc";
			var outpath = PathName.tmp +/+ "SC_GOrganisms"
			+/+ UniqueID.next ++ ".wav";

			score.add([
				0, buffer0Copy.allocMsg,
				buffer0Copy.readMsg(buffer0.path);
			]);

			score.add([
				0, buffer1Copy.allocMsg,
				buffer1Copy.readMsg(buffer1.path);
			]);

			score.add([
				0, [\d_recv, SynthDef.new(\GOMutator_EatingSynth, {
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

					var randSig = LFNoise2.ar(LFNoise1.kr(morphRateRate)
						.range(morphRateRate * 2,
							morphRateRate * 2 * \morphRateHi.kr(10.5))
					).unipolar(1);

					var wipeMax = \wipeMax.kr(0.1);

					var choiceArray = [

						PV_BinWipe(chain0, chain1, randSig * 2 - 1 * wipeMax),
						PV_Morph(chain0, chain1,
							LFNoise2.ar(LFNoise1.kr(morphRateRate)
								.range(morphRateRate * 2,
									morphRateRate * 2
									* \morphRateHi.kr(10.5))).unipolar(wipeMax);
						)
					];

					var choiceRateRate = \choiceRateRate.kr(0.5);

					var which = LFNoise2.kr(LFNoise1.kr(choiceRateRate)
						.range(choiceRateRate * 2,
							choiceRateRate * 2 * \choiceRateHi.kr(10.5))
					).unipolar(1) * (choiceArray.size - 1);

					var chain = Select.kr(which, choiceArray);

					var out = IFFT(chain) * \ampDB.kr(-3).dbamp;
					out = LeakDC.ar(out);
					out = LPF.ar(out, 16000);
					out = HPF.ar(out, 20);
					Out.ar(\out.kr(0), out * EnvGen.ar(
						Env([0, 1, 1, 1, 0], [0.025, 1, 1, 0.025]
							.normalizeSum, \lin),
						timeScale: timescale,
						doneAction: Done.freeSelf
					));
				}).asBytes]
			]);

			score.add([
				0, (synth = Synth.basicNew(\GOMutator_EatingSynth,
					server)).newMsg(args: [
					\timescale, timescale,
					\buf0, buffer0Copy,
					\buf1, buffer1Copy,

					\rate0, 1,
					\rate1, rrand(0.9, 1.1),

					\morphRateRate, timescale * exprand(1.05, 6.0),
					\morphRateHi, exprand(1.05, 6.0),

					\choiceRateRate, timescale * exprand(1.5, 3.0),
					\choiceRateHi, exprand(2.0, 6.0),

					\ampDB, 6,

					\wipeMax, exprand(0.1, 0.5)
				]);
			]);

			score.add([timescale, 0]);

			score.sort;

			score.recordNRT(
				oscpath,
				outpath,
				nil,
				48e3,
				"wav",
				"int24",
				ServerOptions.new
				.numInputBusChannels_(0)
				.numOutputBusChannels_(1)
				.sampleRate_(48e3)
				.memSize_(2.pow(19))
				.numWireBufs_(2.pow(13))
				.verbosity_(-2),
				action: {

					fork{

						var localCondition = Condition.new;
						var toReturn;

						buffer0Copy.freeMsg;
						buffer1Copy.freeMsg;

						File.delete(oscpath);

						toReturn = Buffer.read(server, outpath, action: {
							localCondition.unhang;
						});

						localCondition.hang;
						return.value = toReturn;

						action.value;

					}

				}

			);

			^return;

		}/*ELSE*/{
			this.pr_GOMutatorInit;
			this.mateBuffers(buffer0, buffer1, action);
		};

	}
}