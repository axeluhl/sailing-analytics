package com.sap.sailing.domain.racelog;

import com.sap.sailing.domain.tracking.Track;


public interface RaceLog extends Track<RaceLogEvent> {
	
	void add(RaceLogEvent event);
	
	void addListener(RaceLogListener newListener);

}
