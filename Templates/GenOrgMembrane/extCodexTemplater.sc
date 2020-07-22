+ CodexTemplater { 

	membranePath { ^PathName(this.filenameString).pathOnly }

	genOrgTemplate { | object, name | 
		this.makeExtTemplate(
			this.genOrgPath, 
			name ?? { object }, 
			object
		)
	}

	nucleusSynthDef { 
		this.makeExtTemplate(
			"synthDef", 
			"nucleusSynthDef", 
			this.membranePath
		);
	}

	monoMembrane { 
		this.makeExtTemplate( 
			"signalFunction", 
			"monoMembrane", 
			this.membranePath
		)
	}
	
	stereoMembrane { 
		this.makeExtTemplate( 
			"signalFunction", 
			"stereoMembrane", 
			this.membranePath
		)
	}
	
	quadMembrane { 
		this.makeExtTemplate( 
			"signalFunction", 
			"quadMembrane", 
			this.membranePath
		)
	}
	
	foaMembrane { 
		this.makeExtTemplate( 
			"signalFunction", 
			"foaMembrane", 
			this.membranePath
		)
	}

	hoaMembrane { 
		this.makeExtTemplate( 
			"signalFunction", 
			"hoaMembrane", 
			this.membranePath
		)
	}
}
