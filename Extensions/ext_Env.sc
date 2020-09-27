+ Env {
	+ { | to |
		^Env(
			this.levels + to.levels,
			this.times + to.times,
			this.curves + to.curves
		);
	}

	/ { | val |
		var newLevels, newTimes, newCurves;
		case { val.isKindOf(SimpleNumber) }{
			newLevels = this.levels / val;
			newTimes = this.times / val;
			newCurves = this.curves / val;
		}
		{ val.isKindOf(Env) }{
			newLevels = this.levels / val.levels;
			newTimes = this.times / val.times;
			newCurves = this.curves / val.curves;
		} { ^(this / val) };

		^Env(newLevels, newTimes, newCurves);
	}
}
