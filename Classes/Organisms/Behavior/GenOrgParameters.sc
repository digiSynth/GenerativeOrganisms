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

	mutateWith { | target |  
		var child = GenOrgParameters.new(\mutation);
		var tmodules = target.modules;
		modules.keysValuesDo({ | key, item | 
			var tarItem = tmodules[key];
			child[key] = [ 
				item.minval + tarItem.minval / 2, 
				item.maxval + tarItem.maxval / 2;
				[item.warp + tarItem.warp].choose;
			].asSpec;
		});
		^child;
	}
}
