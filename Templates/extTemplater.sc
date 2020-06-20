+ Templater { 

	nucleusPath { 
		^(Main.packages.asDict.at('GenOrg')+/+"Templates");
	}

	genOrgTemplate { | object | 
		this.makeExtTemplate(this.nucleusPath, 
			"nucleusFunction", object);
	}

	nucleusShell { 
		this.makeExtTemplate(
			this.nucleusPath, 
			"nucleusShell", 
			"nucleusShell"
		);
	}

	monoNucleus { this.genOrgTemplate("monoNucleus"); }

	stereoNucleus { this.genOrgTemplate("stereoNucleus"); }

	quadNucleus { this.genOrgTemplate("quadNucleus"); }

	foaNucleus { this.genOrgTemplate("foaNucleus"); }

	hoaNucleus { this.genOrgTemplate("hoaNucleus"); }

	behaviorSynthDef { this.genOrgTemplate("behaviorSynthDef"); }

	behaviorArgs { this.genOrgTemplate("behaviorArgs"); }

	behaviorEnvs { this.genOrgTemplate("behaviorEnvs"); }
}
