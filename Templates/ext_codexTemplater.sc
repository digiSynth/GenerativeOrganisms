+ CodexTemplater { 

	genOrg_path { 
		^Main.packages.asDict.at(\GenOrg)+/+"Templates";
	}

	membrane_function { name("membrane_function")
		this.makeTemplate(
			name, 
			this.genOrg_path+/+"membrane_function.scd"
		);
	}

}
