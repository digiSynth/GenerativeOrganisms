//Maybe don't edit this module...
{ | modules, key, envs| 
	var wrapperEnvs = ();  
	envs.keysValuesDo({ | key, value |
		wrapperEnvs.add(key -> {  | timescale(\timescale.kr(1)) |
			SynthDef.wrap({ 
				var lo = format("%lo", key).asSymbol.kr(0); 
				var hi = format("%hi", key).asSymbol.kr(1); 
				EnvGen.ar(env, timeScale: timescale)
				.range(lo, hi);
			})
		});
	});
}
