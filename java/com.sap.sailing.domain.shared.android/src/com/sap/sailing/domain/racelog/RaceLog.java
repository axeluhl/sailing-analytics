package com.sap.sailing.domain.racelog;

import com.sap.sailing.domain.tracking.Track;

public interface RaceLog extends Track<RaceLogEvent> {
	
	boolean add(RaceLogEvent event);
	
	void addListener(RaceLogEventVisitor listener);

}
