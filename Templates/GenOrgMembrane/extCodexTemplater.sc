+ CodexTemplater { 

	membranePath { ^PathName(thisMethod.filenameSymbol.asString).pathOnly }

	membraneFunction { 
		this.makeExtTemplate(
			"function", 
			"membraneFunction", 
			this.membranePath
		);
	}

	monoMembrane { 
		this.makeExtTemplate( 
			"membraneWrap", 
			"monoMembrane", 
			this.membranePath
		)
	}
	
	stereoMembrane { 
		this.makeExtTemplate( 
			"membraneWrap", 
			"stereoMembrane", 
			this.membranePath
		)
	}
	
	quadMembrane { 
		this.makeExtTemplate( 
			"membraneWrap", 
			"quadMembrane", 
			this.membranePath
		)
	}
	
	foaMembrane { 
		this.makeExtTemplate( 
			"membraneWrap", 
			"foaMembrane", 
			this.membranePath
		)
	}

	hoaMembrane { 
		this.makeExtTemplate( 
			"membraneWrap", 
			"hoaMembrane", 
			this.membranePath
		)
	}
}
