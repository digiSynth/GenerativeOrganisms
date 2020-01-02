GOBehaviorOptions{

	classvar <>rateLoMin = 0.095, <>rateLoMax = 0.5;
	classvar <>rateHiMin = 0.501, <>rateHiMax = 16.0;
	classvar <>posLoMin = 0.0, <>posLoMax = 0.5;
	classvar <>posHiMin = 0.5, <>posHiMax = 1.5;
	classvar <>ampLoMin = 0.0, <>ampLoMax = 0.25;
	classvar <>ampHiMin = 0.2501, <>ampHiMax = 1.05;
	classvar <>ffreqLoMin = 80.0, <>ffreqLoMax = 1000.0;
	classvar <>ffreqHiMin = 1001.0, <>ffreqHiMax = 12000.0;
	classvar <>impulseRateLoMin = 5.0, <>impulseRateLoMax = 12.5;
	classvar <>impulseRateHiMin = 12.501, <>impulseRateHiMax = 125.0;
	classvar <>grainDurLoMin = 0.0025, <>grainDurLoMax = 0.05;
	classvar <>grainDurHiMin = 0.055, <>grainDurHiMax = 0.125;
	classvar <>rqLoMin = 0.1, <>rqLoMax = 0.125;
	classvar <>rqHiMin = 0.126, <>rqHiMax = 1.0;
	classvar <>timescaleMin = 0.0625, <>timescaleMax = 4.0;

	var <rateLo, <rateHi;
	var <posLo, <posHi;
	var <ffreqLo, <ffreqHi;
	var <ampLo, <ampHi;
	var <impulseRateLo, <impulseRateHi;
	var <grainDurLo, <grainDurHi;
	var <rqLo, <rqHi;
	var <timescale;
	var <timescaleScalar = 1;

	*new{

		^super.new.pr_NewVROBehaviorOptions;

	}

	pr_NewVROBehaviorOptions{

		rateLo = this.pr_CheckAndScaleMinMax(rateLo, rateLoMin, rateLoMax, 0);
		rateHi = this.pr_CheckAndScaleMinMax(rateHi, rateHiMin, rateHiMax, 1);

		posLo = this.pr_CheckAndScaleMinMax(posLo, posLoMin, posLoMax, 0);
		posHi = this.pr_CheckAndScaleMinMax(posHi, posHiMin, posHiMax, 0);

		ampLo = this.pr_CheckAndScaleMinMax(ampLo, ampLoMin, ampLoMax, 0);
		ampHi = this.pr_CheckAndScaleMinMax(ampHi, ampHiMin, ampHiMax, 1);

		ffreqLo = this.pr_CheckAndScaleMinMax(ffreqLo, ffreqLoMin, ffreqLoMax, 1);
		ffreqHi = this.pr_CheckAndScaleMinMax(ffreqHi, ffreqHiMin, ffreqHiMax, 1);

		impulseRateLo = this.pr_CheckAndScaleMinMax(impulseRateLo,
			impulseRateLoMin, impulseRateLoMax, 0);
		impulseRateHi = this.pr_CheckAndScaleMinMax(impulseRateHi,
			impulseRateHiMin, impulseRateHiMax, 1);

		grainDurLo = this.pr_CheckAndScaleMinMax(grainDurLo, grainDurLoMin, grainDurLoMax, 1);
		grainDurHi = this.pr_CheckAndScaleMinMax(grainDurHi, grainDurHiMin, grainDurHiMax, 1);

		rqLo = this.pr_CheckAndScaleMinMax(rqLo, rqLoMin, rqLoMax, 0);
		rqHi = this.pr_CheckAndScaleMinMax(rqHi, rqHiMin, rqHiMax, 0);

		timescale = this.pr_CheckAndScaleMinMax(timescale, timescaleMin, timescaleMax, 1);
		// timescale = exprand(0.1, 1.5);
	}

	pr_CheckAndScaleMinMax{|item, min, max, how = 0|
		var return;

		if(item.isNil){

			if(how==0){

				item = rrand(min, max);

			}/*ELSE*/{

				if(how==1){
					item = exprand(min, max);
				};

			};

		}/*ELSE*/{

			if(item < min){

				item = min;

			};

			if(item > max){

				item = max;

			};

		};

		return = item;

		^return;
	}

/*	printOn{|stream|

		stream<<"a GOBehaviorOptions\n"
		<<"\t>> rateLo: "<<rateLo//<<"\n"
		<<"\t\t>> rateHi: "<<rateHi<<"\n"

		<<"\t>> posLo: "<<posLo//<<"\n"
		<<"\t\t>>posLo: "<<posHi<<"\n"

		<<"\t>> ampLo: "<<ampLo//<<"\n"
		<<"\t\t>>ampHi: "<<ampHi<<"\n"

		<<"\t>> ffreqLo: "<<ffreqLo//<<"\n"
		<<"\t\t>>ffreqHi: "<<ffreqHi<<"\n"

		<<"\t>> impulseRateLo: "<<impulseRateLo//<<"\n"
		<<"\t\t>>impulseRateHi: "<<impulseRateHi<<"\n"

		<<"\t\t>> grainDurLo: "<<grainDurLo//<<"\n"
		<<"\t\t>>grainDurHi: "<<grainDurHi<<"\n"

		<<"\t\t>> rqLo: "<<rqLo//<<"\n"
		<<"\trqHi: "<<rqHi<<"\n"

		<<"\t>> timescale: "<<timescale;

	}*/

	rateLo_{|newVal|

		rateLo = this.pr_CheckAndScaleMinMax(newVal, rateLoMin, rateLoMax, 0);

	}

	rateHi_{|newVal|

		rateHi = this.pr_CheckAndScaleMinMax(newVal, rateHiMin, rateHiMax, 1);

	}

	posLo_{|newVal|

		posLo = this.pr_CheckAndScaleMinMax(newVal, posLoMin, posLoMax, 0);

	}

	posHi_{|newVal|

		posHi = this.pr_CheckAndScaleMinMax(newVal, posHiMin, posHiMax, 0);

	}

	ampLo_{|newVal|

		ampLo = this.pr_CheckAndScaleMinMax(newVal, ampLoMin, ampLoMax, 0);

	}

	ampHi_{|newVal|

		ampHi = this.pr_CheckAndScaleMinMax(newVal, ampHiMin, ampHiMax, 1);

	}

	ffreqLo_{|newVal|

		ffreqLo = this.pr_CheckAndScaleMinMax(newVal, ffreqLoMin, ffreqLoMax, 1);

	}

	ffreqHi_{|newVal|

		ffreqHi = this.pr_CheckAndScaleMinMax(newVal, ffreqHiMin, ffreqHiMax, 1);

	}

	impulseRateLo_{|newVal|

		impulseRateLo = this.pr_CheckAndScaleMinMax(newVal,
			impulseRateLoMin, impulseRateLoMax, 0
		);

	}

	impulseRateHi_{|newVal|

		impulseRateHi = this.pr_CheckAndScaleMinMax(newVal,
			impulseRateHiMin, impulseRateHiMax, 1
		);

	}

	grainDurLo_{|newVal|

		grainDurLo = this.pr_CheckAndScaleMinMax(newVal,
			grainDurLoMin, grainDurLoMax, 1
		);

	}

	grainDurHi_{|newVal|

		grainDurHi = this.pr_CheckAndScaleMinMax(newVal,
			grainDurHiMin, grainDurHiMax, 1
		);

	}

	rqLo_{|newVal|

		rqLo = this.pr_CheckAndScaleMinMax(newVal,
			rqLoMin, rqLoMax, 0
		);

	}

	rqHi_{|newVal|

		rqHi = this.pr_CheckAndScaleMinMax(newVal,
			rqHiMin, rqHiMax, 0
		);

	}

	timescale_{|newVal|

		timescale = this.pr_CheckAndScaleMinMax(newVal,
			timescaleMin, timescaleMax, 1
		);

	}

	timescaleScalar_{|newVal|

		if(newVal > 1.0){

			timescaleScalar = newVal;

		}/*ELSE*/{

			var testingVal = newVal * 3;

			if(testingVal < 1.0){

				timescaleScalar = testingVal;

			}/*ELSE*/{

				timescaleScalar = 1.0;

			};

		};

	}

	averageOptions{|targetOptions|
		var return;

		if(targetOptions.class!=GOBehaviorOptions){
			Error("Can only average a GOBehaviorOptions"
				++" instance with another one.").throw;
		};

		return = GOBehaviorOptions.new;

		return.rateLo = rateLo + targetOptions.rateLo * 0.5;
		return.rateHi = rateHi + targetOptions.rateHi * 0.5;
		return.posLo = posLo + targetOptions.posLo * 0.5;
		return.posHi = posHi + targetOptions.posHi * 0.5;
		return.ampLo = ampLo + targetOptions.ampLo * 0.5;
		return.ampHi = ampHi + targetOptions.ampHi * 0.5;
		return.ffreqLo = ffreqLo + targetOptions.ffreqLo * 0.5;
		return.ffreqHi = ffreqHi + targetOptions.ffreqHi * 0.5;
		return.impulseRateLo = impulseRateLo + targetOptions.impulseRateLo * 0.5;
		return.impulseRateHi = impulseRateHi + targetOptions.impulseRateHi * 0.5;
		return.grainDurLo = grainDurLo + targetOptions.grainDurLo * 0.5;
		return.grainDurHi = grainDurHi + targetOptions.grainDurHi * 0.5;
		return.rqLo = rqLo + targetOptions.rqLo * 0.5;
		return.rqHi = rqHi + targetOptions.rqHi * 0.5;
		return.timescale = timescale + targetOptions.timescale * 0.5;

		^return;
	}
}