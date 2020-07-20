//Maybe don't edit this module too much...
{ | modules |
	var envs = ();
	modules.behaviorArgs.keysDo.do({ | key |
		var n = exprand(4, 32).round(2);
		var levels = Array.fill(n, { 1.0.rand });
		var times = Array.fill(n - 1, { exprand(0.01, 1.0) });
		var curves = Array.fill(n - 1, { rrand(-12, 12) });
		envs.add(key -> Env(levels, times.normalizeSum, curves));
	});
	envs;
};
