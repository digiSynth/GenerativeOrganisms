+ CodexTemplater {
	behaviorPath { ^PathName(thisMethod.filenameString).pathOnly }

	behaviorArgs {
		this.makeExtTemplate(
			"args",
			"behaviorArgs",
			this.behaviorPath
		);
	}

	behaviorEnvs {
		this.makeExtTemplate(
			"envs",
			"behaviorEnvs",
			this.behaviorPath
		);
	}

	behaviorEnvsWrappers {
		this.makeExtTemplate(
			"wrappers",
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
