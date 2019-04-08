package com.sap.sailing.server.gateway.serialization.racelog.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLogEndOfTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceLogEndOfTrackingEventSerializer extends BaseRaceLogEventSerializer implements
        JsonSerializer<RaceLogEvent> {
    
    public static final String VALUE_CLASS = RaceLogEndOfTrackingEvent.class.getSimpleName();

    public RaceLogEndOfTrackingEventSerializer(JsonSerializer<Competitor> competitorSerializer) {
        super(competitorSerializer);
    }

    @Override
    protected String getClassFieldValue() {
        return VALUE_CLASS;
    }

}
