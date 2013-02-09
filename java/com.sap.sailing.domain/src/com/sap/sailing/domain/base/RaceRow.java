package com.sap.sailing.domain.base;

public interface RaceRow {
	
	Fleet getFleet();
	
	Iterable<RaceCell> getCells();

}
