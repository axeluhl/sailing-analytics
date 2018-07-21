package com.sap.sailing.xrr.structureimport;

import com.sap.sailing.domain.base.impl.DynamicBoat;
import com.sap.sailing.domain.base.impl.DynamicTeam;



public class BoatAndTeam {
	private DynamicBoat boat = null;
	private DynamicTeam team = null;
	
	public BoatAndTeam(DynamicBoat boat, DynamicTeam team){
		this.boat = boat;
		this.team = team;
	}
	
	public BoatAndTeam(){
		
	}
	
	public DynamicBoat getBoat() {
		return boat;
	}
	public void setBoat(DynamicBoat boat) {
		this.boat = boat;
	}
	public DynamicTeam getTeam() {
		return team;
	}
	public void setTeam(DynamicTeam team) {
		this.team = team;
	}
}
