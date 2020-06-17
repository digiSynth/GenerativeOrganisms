+ Templater { 

	nucleusFunctionPath { 
		^(Main.packages.asDict.at('GenOrg')+/+"Templates");
	}

	nucleusTemplate { | object("nucleusFunction") | 
		this.makeExtTemplate(this.nucleusFunctionPath, object, object);
	}

	nucleusShell { this.nucleusTemplate("nucleusShell"); }

	monoNucleus { this.nucleusTemplate("monoNucleus"); }

	stereoNucleus { this.nucleusTemplate("stereoNucleus"); }

	quadNucleus { this.nucleusTemplate("quadNucleus"); }

	foaNucleus { this.nucleusTemplate("foaNucleus"); }

	hoaNucleus { this.nucleusTemplate("hoaNucleus"); }

}
