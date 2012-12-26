package com.sap.sailing.domain.racecommittee;

import com.sap.sailing.domain.tracking.Track;


public interface RaceCommitteeEventTrack extends Track<RaceCommitteeEvent> {
	
	void add(RaceCommitteeEvent event);
	
	void addListener(RaceCommitteeListener newListener);

}
