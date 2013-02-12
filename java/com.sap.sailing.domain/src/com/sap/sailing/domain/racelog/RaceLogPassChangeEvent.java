package com.sap.sailing.domain.racelog;

public interface RaceLogPassChangeEvent extends RaceLogEvent {
	
	int getNewPassId();

}
