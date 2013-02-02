package com.sap.sailing.domain.racecommittee;

import java.io.Serializable;

public interface RaceCommitteeCourseAreaChangedEvent extends RaceCommitteeEvent {
	
	Serializable getCourseAreaId();

}
