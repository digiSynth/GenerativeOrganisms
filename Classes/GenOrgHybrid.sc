GenOrgHybrid : CodexHybrid { 
	
	initHybrid { 
		this.addSynthDef;
		this.initGenOrgHybrid;
	}

	initGenOrgHybrid {  }

	*addModules { | key |
		this.cache.add(key -> this.loadScripts(key));
	}

	addSynthDef {
		var cache = this.class.cache.at(moduleSet);
		if(cache.at(\synthDef).isNil, { 
			var synthDef = this.buildSynthDef;
			cache.add(\synthDef -> synthDef);
			modules.add(\synthDef -> synthDef);
			this.class.processSynthDefs(moduleSet);
		});
	}

	buildSynthDef { 
		this.subclassResponsibility(thisMethod);
	}
}
