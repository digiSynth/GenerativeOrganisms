GOCurve{
	classvar shapeNames, isInit = false;
	var <env, <min, <max;
	var <isUnscaled = true;

	*new{ |env, min, max|

		env = env ? Env();

		this.pr_GOCurveInit;
		this.prCheckEnv(env);
		^super.new.pr_MakeGOrganismCurve(env, min, max);
	}

	*pr_GOCurveInit{
		if(isInit==false){
			shapeNames = IdentityDictionary[
				\step -> 0,
				\lin -> 1,
				\linear -> 1,
				\exp -> 2,
				\exponential -> 2,
				\sin -> 3,
				\sine -> 3,
				\wel -> 4,
				\welch -> 4,
				\sqr -> 6,
				\squared -> 6,
				\cub -> 7,
				\cubed -> 7,
				\hold -> 8,
			];
			shapeNames.freeze;
		}
	}

	pr_MakeGOrganismCurve{ |inenv, inmin, inmax|

		env = inenv;

		if(env.times.size != (env.levels.size - 1)){
			env.times = env.times.resize(env.levels.size - 1);
		};

		if(env.times.sum != 1.0){
			env.times = env.times.normalizeSum;
		};

		if(inmin.isNil.not and: {inmax.isNil.not}){
			var lminmax = env.levels.minMax;

			min = inmin;
			max = inmax;

			env.levels = env.levels
			.linlin(lminmax.min, lminmax.max, min, max);
		};

		if(inmax.isNil.not or: {inmin.isNil.not}){
			isUnscaled = false;
		};

		if(inmin.isNil){
			min = env.levels.min;
		};

		if(inmax.isNil){
			max = env.levels.max;
		};

	}

	*prCheckEnv{|inenv|
		inenv ?? {
			Error("Input must be an envelope").throw;
		};

		inenv !? {
			if(inenv.class!=Env){
				Error("Input must be an envelope").throw;
			}
		}
	}

	min_{|input|
		min = input;

		if(env.isNil.not){
			var lminmax = env.levels.minMax;
			if(max.isNil.not){

				env.levels = env.levels
				.linlin(lminmax.min, lminmax.max, min, max);

			}/*ELSE*/{

				env.levels = env.levels
				.linlin(lminmax.min, lminmax.max, min, lminmax.max);

			}
		};

		isUnscaled = false;
	}

	max_{|input|
		max = input;

		if(env.isNil.not){
			var lminmax = env.levels.minMax;
			if(min.isNil.not){
				env.levels = env.levels
				.linlin(lminmax.min, lminmax.max, min, max);
			}/*ELSE*/{
				env.levels = env.levels
				.linlin(lminmax.min, lminmax.max, lminmax.min, max);
			}
		};

		isUnscaled = false;
	}

	env_{|input|
		env = input;

		if(env.times.size != (env.levels.size - 1)){
			env.times = env.times.resize(env.levels.size - 1);
		};

		if(env.times.sum != 1.0){
			env.times = env.times.normalizeSum;
		};

		case
		{(min.isNil.not) and: {max.isNil.not}}{
			var lminmax = env.levels.minMax;

			env.levels = env.levels
			.linlin(lminmax.min, lminmax.max, min, max);

			isUnscaled = false;
		}

		{(min.isNil.not) and: {max.isNil}}{
			var lminmax = env.levels.minMax;

			env.levels = env.levels
			.linlin(lminmax.min, lminmax.max, min, lminmax.max);

			isUnscaled = false;
		}

		{(min.isNil) and: {max.isNil.not}}{
			var lminmax = env.levels.minMax;

			env.levels = env.levels
			.linlin(lminmax.min, lminmax.max, lminmax.min, max);

			isUnscaled = true;
		};

	}

	averageCurves{ |curve|
		var returnEnv;
		var thisLevels, thatLevels, averageLevels;

		var thisTimes, thatTimes, averageTimes;

		var thisCurves, thatCurves, averageCurves;

		var averageMin, averageMax;

		var returnGOCurve;


		if(curve.class!=GOCurve){
			Error("Can only average GOCurves with other GOCurves.").throw;
		};

		//levels stuff
		thisLevels = env.levels;
		thatLevels = curve.env.levels;

		if(thisLevels.size > thatLevels.size){
			thatLevels = thatLevels.resize(thisLevels.size);
		};

		if(thatLevels.size > thisLevels.size){
			thisLevels = thisLevels.resize(thatLevels.size);
		};

		//time stuff
		thisTimes = env.times;
		thatTimes = curve.env.times;


		if(thisTimes.size > thatTimes.size){
			thatTimes = thatTimes.resize(thisTimes.size);
		};

		if(thatTimes.size > thisTimes.size){
			thisTimes = thisTimes.resize(thatTimes.size);
		};


		//curve stuff
		thisCurves = env.curves;
		thatCurves = curve.env.curves;

		if(thisCurves.isCollection.not){
			thisCurves = {shapeNames[thisCurves]} ! (env.levels.size - 1);
		};

		if(thatCurves.isCollection.not){
			thatCurves = {shapeNames[thatCurves]} ! (curve.env.levels.size - 1);
		};

		if(thisCurves.size > thatCurves.size){
			thatCurves = thatCurves.resize(thisCurves.size);
		};

		if(thatCurves.size > thisCurves.size){
			thisCurves = thisCurves.resize(thatCurves.size);
		};

		averageLevels = thisLevels + thatLevels / 2;
		averageTimes = thisTimes + thatTimes / 2;
		averageCurves = thisCurves + thatCurves / 2;

		returnEnv = Env(averageLevels, averageTimes, averageCurves);
		averageMin = min + curve.min / 2;
		averageMax = max + curve.max / 2;

		returnGOCurve = GOCurve(returnEnv, averageMin, averageMax);
		^returnGOCurve;
	}

	plot{
		^env.plot;
	}

}
