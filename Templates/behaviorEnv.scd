var n = exprand(4, 32).round(2); 
var levels = Array.fill(n, { 1.0.rand }); 
var times = Array.fill(n, { 1.0.rand }).normalizeSum; 
var curves = Array.fill(n, { rrand(-12, 12) }); 
Env(levels, times, curves);
