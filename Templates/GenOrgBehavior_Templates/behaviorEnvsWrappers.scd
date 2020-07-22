//Maybe don't edit this module...
{ | modules | 
	var envWrappers = ();
	
	if(modules[\envs].isFunction, { 
		modules[\envs] = modules.envs;
	});

	modules.envs.keysValuesDo({ | key, env |
		envWrappers.add(key -> {  | timescale(\timescale.kr(1)) |
			SynthDef.wrap({ 
				var lo = format("%lo", key).asSymbol.kr(0); 
				var hi = format("%hi", key).asSymbol.kr(1); 
				EnvGen.ar(env, timeScale: timescale)
				.range(lo, hi);
			})
		});
	});
	envWrappers;
}
