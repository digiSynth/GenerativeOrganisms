+ CodexTemplater { 

	genOrgPath { 
		^(Main.packages.asDict.at('GenOrg')+/+"Templates");
	}

	genOrgTemplate { | object, name | 
		this.makeExtTemplate(
			this.genOrgPath, 
			name ?? { object }, 
			object
		)
	}

	nucleusShell { 
		this.makeExtTemplate(
			this.genOrgPath, 
			"nucleusShell", 
			"nucleusShell"
		)
	}

	monoNucleus { this.genOrgTemplate("monoNucleus") }

	stereoNucleus { this.genOrgTemplate("stereoNucleus") }

	quadNucleus { this.genOrgTemplate("quadNucleus") }

	foaNucleus { this.genOrgTemplate("foaNucleus") }

	hoaNucleus { this.genOrgTemplate("hoaNucleus") }

	behaviorSynthDef { this.genOrgTemplate("behaviorSynthDef", "synthDef") }

	behaviorArgs { this.genOrgTemplate("behaviorArgs") }

	behaviorEnvs { this.genOrgTemplate("behaviorEnvs") }

	behaviorEnvWrapper { this.genOrgTemplate("behaviorEnvWrapper") }
}
