+ Templater { 

	nucleusFunctionPath { 
		^(Main.packages.asDict.at('GenOrg')+/+"Templates");
	}

	genOrgTemplate { | object("nucleusFunction") | 
		this.makeExtTemplate(this.nucleusFunctionPath, object, object);
	}

	nucleusShell { this.genOrgTemplate("nucleusShell"); }

	monoNucleus { this.genOrgTemplate("monoNucleus"); }

	stereoNucleus { this.genOrgTemplate("stereoNucleus"); }

	quadNucleus { this.genOrgTemplate("quadNucleus"); }

	foaNucleus { this.genOrgTemplate("foaNucleus"); }

	hoaNucleus { this.genOrgTemplate("hoaNucleus"); }

	behaviorSynthDef { this.genOrgTemplate("behaviorSynthDef"); }

	behaviorArgs { this.genOrgTemplate("behaviorArgs"); }

	behaviorEnvs { this.genOrgTemplate("behaviorEnvs"); }
}
