package com.sap.sailing.domain.racelog.tracking;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.racelog.RaceLogEvent;

public interface RegisterCompetitorEvent extends RaceLogEvent {
	Competitor getCompetitor();
}
