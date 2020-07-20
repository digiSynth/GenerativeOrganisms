+ CodexTemplater { 
	behaviorPaths { ^(thisMethod.filenameString.dirname
	+/+"BehaviorTemplates") }

	behaviorArgs { 
		this.makeExtTemplate(
			"behaviorArgs",
			"behaviorArgs", 
			this.behaviorPath
		);
	}

	behaviorEnvs { 
		this.makeExtTemplate(
			"behaviorEnvs", 
			"behaviorEnvs", 
			this.behaviorPath
		);
	}

	behaviorEnvsWrappers { 
		this.makeExtTemplate(
			"behaviorEnvsWrappers",
			"behaviorEnvsWrappers",
			this.behaviorPath
		);
	}

	behaviorSynthDef { 
		this.makeExtTemplate(
			"synthDef", 
			"behaviorSynthDef", 
			this.behaviorPath
		);
	}
}
