+ CodexTemplater {
	genOrgPath { ^(Main.packages.asDict.at(\GenOrg)+/+"Templates") }

	genOrgVoicer { | templateName("voicer") |
		this.makeTemplate(templateName, this.genOrgPath+/+"voicer.scd");
	}

	genOrgSpatializer { | templateName("spatializer") |
		this.makeTemplate(templateName, this.genOrgPath+/+"spatializer.scd");
	}

	genOrgMutator { | templateName("mutator") |
		this.makeTemplate(templateName, this.genOrgPath+/+"mutator.scd");
	}
}