package com.sap.sailing.server.gateway.serialization.racelog.impl;

import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogStartTrackingEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceLogStartTrackingEventSerializer extends BaseRaceLogEventSerializer {
    public static final String VALUE_CLASS = RaceLogStartTrackingEvent.class.getSimpleName();

    public RaceLogStartTrackingEventSerializer(
            JsonSerializer<Competitor> competitorSerializer) {
        super(competitorSerializer);
    }

    @Override
    protected String getClassFieldValue() {
        return VALUE_CLASS;
    }

}
