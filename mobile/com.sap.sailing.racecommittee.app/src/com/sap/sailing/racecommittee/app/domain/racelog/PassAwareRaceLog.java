package com.sap.sailing.racecommittee.app.domain.racelog;

import com.sap.sailing.domain.racelog.RaceLog;

public interface PassAwareRaceLog extends RaceLog {
	int getCurrentPassId();
}
