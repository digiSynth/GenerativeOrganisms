VROrganismSystemConfigurer{
	classvar <>filePath, <oscAddresses;
	classvar <prependedAddresses;
	classvar <prependageAddress;

	classvar <spawnAddress, <positionAddress, <mateAddress;
	classvar <killAddress, <soundAddress, <eatAddress;
	classvar <awakeAddress, <enabledAddress, <disabledAddress;

	classvar extensionOrQuark = "Extensions";

	*start{

		filePath = filePath ? (Platform.userExtensionDir
			++"/GenerativeOrganisms/Systems/VR/VROSCAddresses.YAML");

		if(File.exists(filePath)){
			oscAddresses = filePath.parseYAMLFile;
			prependageAddress = oscAddresses["prependageAddress"];
			oscAddresses.removeAt("prependageAddress");

			if(prependageAddress.asString != "nil"){
				prependedAddresses = oscAddresses.collect({|item|
					item = item.asString;
					item = format("/%", prependageAddress)++item;
					item.asSymbol;
				});

				this.pr_SetupVROSCDefs(prependedAddresses);
			}/*ELSE*/{
				this.pr_SetupVROSCDefs(oscAddresses);
			};

		}/*ELSE*/{
			this.configure;
			this.start;
		}
	}

	*pr_SetupVROSCDefs{|addressDictionary|
		//This makes the whole class abstract!
		this.subclassResponsibility(thisMethod);
	}

	*configure{|path, prependageAddress,
		spawnAddress, positionAddress,
		killAddress, soundAddress,
		awakeAddres, enabledAddress,
		disabledAddress, eatAddress,
		mateAddress|

		var file; var filestring = "";

		filePath = path ? (Platform.userExtensionDir
			++"/GenerativeOrganisms/Systems/VR/VROSCAddresses.YAML");

		this.prependageAddress = prependageAddress ? "nil";
		this.spawnAddress = spawnAddress ? '/spawn';
		this.positionAddress = positionAddress ? '/position';
		this.killAddress = killAddress ? '/kill';
		this.soundAddress = soundAddress ? '/sound';
		this.awakeAddress = awakeAddress ? '/awake';
		this.enabledAddress = enabledAddress ? '/enabled';
		this.disabledAddress = disabledAddress ? '/disabled';
		this.eatAddress = eatAddress ? '/eat';
		this.mateAddress = mateAddress ? '/mate';

		oscAddresses = Dictionary.new;
		oscAddresses[\prependageAddress] = this.prependageAddress;
		oscAddresses[\spawn] = this.spawnAddress;
		oscAddresses[\position] = this.positionAddress;
		oscAddresses[\kill] = this.killAddress;
		oscAddresses[\sound] = this.soundAddress;
		oscAddresses[\awake] = this.awakeAddress;
		oscAddresses[\enabled] = this.enabledAddress;
		oscAddresses[\disabled] = this.disabledAddress;
		oscAddresses[\eat] = this.eatAddress;
		oscAddresses[\mate] = this.mateAddress;

		oscAddresses.keys.do{|item, index|
			var string;

			if(index > 0){
				string = format("\n%: %", item, oscAddresses[item]);
			}/*ELSE*/{
				string = format("%: %", item, oscAddresses[item]);
			};

			filestring = filestring++string;
		};

		file = File.open(filePath, "w");
		file.write(filestring);
		file.close;
	}

	//This crashes everything...so don't do it.

	// *prependageAddress_{ |newAddress|
	// 	prependageAddress = newAddress;
	// 	this.configure;
	// }
	//
	// *spawnAddress_{ |newAddress|
	// 	spawnAddress = newAddress;
	// 	this.configure;
	// }
	//
	// *mateAddress_{ |newAddress|
	// 	mateAddress = newAddress;
	// 	this.configure;
	// }
	//
	// *killAddress_{ |newAddress|
	// 	killAddress = newAddress;
	// 	this.configure;
	// }
	//
	// *soundAddress_{ |newAddress|
	// 	soundAddress = newAddress;
	// 	this.configure;
	// }
	//
	// *eatAddress_{ |newAddress|
	// 	eatAddress = newAddress;
	// 	this.configure;
	// }

}