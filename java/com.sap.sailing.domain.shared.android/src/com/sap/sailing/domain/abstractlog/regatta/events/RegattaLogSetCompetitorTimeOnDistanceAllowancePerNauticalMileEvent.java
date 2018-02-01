package com.sap.sailing.domain.abstractlog.regatta.events;

import com.sap.sse.common.Duration;

public interface RegattaLogSetCompetitorTimeOnDistanceAllowancePerNauticalMileEvent extends RegattaLogSetCompetitorHandicapInfoEvent {
    Duration getTimeOnDistanceAllowancePerNauticalMile();
}
