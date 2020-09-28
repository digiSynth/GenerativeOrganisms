GenOrgCell {    
	var buffer, nucleus, membrane, cilium, mutator, <>server; 

	*new { | buffer, nucleus, membrane, mutator, cilium, server(Server.default) |
		^super.newCopyArgs(buffer, nucleus, membrane, mutator, cilium, server);
	}

	mate { | organism |
		if(organism.isKindOf(GenOrgCell), { 


		});
	}

	eat { | organism |
		if(organism.isKindOf(GenOrgCell), { 

		});
	}

	playOrg { | target(server.defaultGroup) |
		server.bind({ 
			membrane.playMembrane;
			
			
		})
	}

}
