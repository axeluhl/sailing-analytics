package com.sap.sailing.domain.racelog;

public interface RaceLogFlagEvent extends RaceLogEvent {
	
	Flags getUpperFlag();
	
	Flags getLowerFlag();
	
	boolean isDisplayed();
}
