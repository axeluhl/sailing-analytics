package com.sap.sailing.domain.base;


public interface FleetWithRaceNames extends Fleet {

	public Iterable<String> getRaceNames();
	
}
