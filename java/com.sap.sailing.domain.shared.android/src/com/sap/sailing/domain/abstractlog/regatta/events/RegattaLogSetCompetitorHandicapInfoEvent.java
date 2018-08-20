package com.sap.sailing.domain.abstractlog.regatta.events;

import com.sap.sailing.domain.abstractlog.Revokable;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.base.Competitor;

public interface RegattaLogSetCompetitorHandicapInfoEvent extends RegattaLogEvent, Revokable {
    Competitor getCompetitor();
}
