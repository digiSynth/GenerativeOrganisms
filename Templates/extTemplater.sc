+ Templater {

	genOrgPath {
		^(Main.packages.asDict.at('GenOrg')+/+"Templates");
	}

	nucleusTemplate { | object |
		this.makeExtTemplate(
			this.genOrgPath,
			"nucleusFunction",
			object
		);
	}

	nucleusShell {
		this.makeExtTemplate(
			this.genOrgPath,
			"nucleusShell",
			"nucleusShell"
		);
	}

	behaviorParameters {
		this.makeExtTemplate(
			this.genOrgPath,
			"parameters",
			"behaviorParameters"
		);
	}

	behaviorEnvs {
		this.makeExtTemplate(
			this.genOrgPath,
			"envs",
			"behaviorEnvs",
		);
	}

	behaviorSynthDef {
		this.makeExtTemplate(
			this.genOrgPath,
			"synthDef",
			"behaviorSynthDef"
		);
	}

	monoNucleus { this.nucleusTemplate("monoNucleus"); }

	stereoNucleus { this.nucleusTemplate("stereoNucleus"); }

	quadNucleus { this.nucleusTemplate("quadNucleus"); }

	foaNucleus { this.nucleusTemplate("foaNucleus"); }

	hoaNucleus { this.nucleusTemplate("hoaNucleus"); }

}
