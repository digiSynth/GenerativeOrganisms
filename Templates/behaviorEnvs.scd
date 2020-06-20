{ | modules |
	var envs = ();
	modules.behaviorArgs.keysDo({ | key |
		var n = exprand(4, 32).round(2);
		var levels = Array.fill(n, { 1.0.rand });
		var times = Array.fill(n - 1, { exprand(0.01, 1.0) });
		var curves = Array.fill(n - 1, { rrand(-12, 12) });
		envs.add(key -> {  | event, timescale |
			SynthDef.wrap({
				var lo = format("%lo", key).asSymbol.kr(0);
				var hi = format("%hi", key).asSymbol.kr(1);
				EnvGen.ar(
					Env(levels, times.normalizeSum, curves),
					timeScale: timescale
				).range(lo, hi);
			});
		});
	});
	envs;
};
