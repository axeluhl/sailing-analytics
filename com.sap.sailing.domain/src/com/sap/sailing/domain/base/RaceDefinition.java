package com.sap.sailing.domain.base;

public interface RaceDefinition extends Named {
	Course getCourse();
	Iterable<Boat> getBoats();
}
