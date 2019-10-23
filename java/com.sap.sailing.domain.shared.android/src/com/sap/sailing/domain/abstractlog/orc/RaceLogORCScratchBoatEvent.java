package com.sap.sailing.domain.abstractlog.orc;

import com.sap.sailing.domain.abstractlog.Revokable;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.base.Competitor;

public interface RaceLogORCScratchBoatEvent extends RaceLogEvent, Revokable {
    default Competitor getCompetitor() {
        return getInvolvedCompetitors().get(0);
    }
}
