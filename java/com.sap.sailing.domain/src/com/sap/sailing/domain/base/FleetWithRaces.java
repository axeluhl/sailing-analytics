package com.sap.sailing.domain.base;


public interface FleetWithRaces extends Fleet {

	public Iterable<RaceDefinition> getRaceDefinitions();
	
}
