package com.sap.sailing.racecommittee.app.domain.state;

import com.sap.sailing.domain.racelog.RaceLogEvent;

public interface RaceLogChangedListener {
	public void eventAdded(RaceLogEvent event);
}
