+ CodexTemplater { 

	genOrg_path { 
		^Main.packages.asDict.at(\GenOrg)+/+"Templates";
	}

	membrane_function { | templateName("membrane_function") |
		this.makeTemplate(
			templateName, 
			this.genOrg_path+/+"membrane_function.scd"
		);
	}

	mutator_function { | templateName("mutator_function") |
		this.makeTemplate(
			templateName, 
			this.genOrg_path+/+"mutator_function.scd"
		);
	}

	cellular_function { | templateName("cellular_function") |
		this.makeTemplate(
			templateName, 
			this.genOrg_path+/+"cellular_function.scd"
		);
	}

}
