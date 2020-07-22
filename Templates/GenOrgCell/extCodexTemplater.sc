+ CodexTemplater {
	cellularPath { ^PathName(thisMethod.filenameString).pathOnly }

	cellularArgs {
		this.makeExtTemplate(
			"args",
			"cellularArgs",
			this.cellularPath
		);
	}

	cellularEnvs {
		this.makeExtTemplate(
			"envs",
			"cellularEnvs",
			this.cellularPath
		);
	}

	cellularWrappers {
		this.makeExtTemplate(
			"wrappers",
			"cellularWrappers",
			this.cellularPath
		);
	}

	cellularFunction {
		this.makeExtTemplate(
			"function",
			"cellularFunction",
			this.cellularPath
		);
	}
}
