+ Templater { 

	nucleusShell { |templateName("nucleusShell")| 
		this.setTemplateDir(PathName(this.filenameString));
		this.makeTemplate(templateName, "nucleusShell");
		this.resetTemplateDir;
	}

	nucelusFunction { | templateName("nucleusFunction") | 
		this.setTemplateDir(PathName(this.filenameString));
		this.makeTemplate(templateName, "nucleusFunction");
		this.resetTemplateDir;
	}

}
