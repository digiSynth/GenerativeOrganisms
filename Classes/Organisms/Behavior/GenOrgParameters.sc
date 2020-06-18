GenOrgParameters : Composite { 

	templater { 
		GenOrgBehavior.arguments.do { | argument |
			templater.parameterSpec(argument.asString);
		}
	}

	at { | argument | ^modules.at(argument); }

	getSynthArgs { 
		var args = modules.copy; 
		args.keys.do{ | key | args[key] = args[key].map(1.0.rand) } 
		^args.asPairs;
	}

	mutateWith {  }
}

GenOrgParMutant {  }
