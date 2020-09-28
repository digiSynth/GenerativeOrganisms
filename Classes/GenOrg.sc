GenOrg {    
	var buffer, cell, membrane, cilium, mutator, <>server; 

	*new { | buffer, cell, membrane, mutator, cilium, server(Server.default) |
		^super.newCopyArgs(buffer, cell, membrane, mutator, cilium, server);
	}

	mate { | organism |
		if(organism.isKindOf(GenOrg), { 


		});
	}

	eat { | organism |
		if(organism.isKindOf(GenOrg), { 

		});
	}

	playOrg { | target(server.defaultGroup) |
		server.bind({ 
			membrane.playMembrane;
			
			
		})
	}

}
