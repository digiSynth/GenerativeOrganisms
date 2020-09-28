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

	gene_function { | templateName("gene_function") |
		this.makeTemplate(
			templateName, 
			this.genOrg_path+/+"gene_function.scd"
		);
	}

	nucleus_function { | templateName("nucleus_function") |
		this.makeTemplate(
			templateName, 
			this.genOrg_path+/+"nucleus_function.scd"
		);
	}

	cilia_function { | templateName("cilia_function") |
		this.makeTemplate(
			templateName, 
			this.genOrg_path+/+"cilia_function.scd"
		);
	}

}
