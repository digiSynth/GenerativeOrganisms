//Fill a dictionary with control specs with keys corresponding to Synth arguments.
var dict = Dictionary.new; 
dict.add(\rate -> ControlSpec(0.05, 9.0, \exp)); 
dict.add(\pos -> ControlSpec(0.0, 1.0)); 
dict.add(\amp -> ControlSpec(0.2, 1.0)); 
dict.add(\ffreq -> ControlSpec(120.0, 16000.0, \exp)); 
dict.add(\impulseRate -> ControlSpec(12.0, 256.0, \exp)); 
dict.add(\grainDur -> ControlSpec(0.05, 8.0, \exp)); 
dict.add(\rq -> ControlSpec(0.09, 0.8, \exp));
dict.add(\timescale -> ControlSpec(0.05, 2.0, \exp));

//return
dict;
