+ Templater { 

	nucleusShell { |templateName("nucleusShell")| 
		this.setTemplateDir(PathName(this.filenameString));
		this.makeTemplate(templateName, "nucleusShell");
		this.resetTemplateDir;
	}

	nucleusFunction { | templateName("nucleusFunction") | 
		this.setTemplateDir(PathName(this.filenameString));
		this.makeTemplate(templateName, "nucleusFunction");
		this.resetTemplateDir;
	}

}
