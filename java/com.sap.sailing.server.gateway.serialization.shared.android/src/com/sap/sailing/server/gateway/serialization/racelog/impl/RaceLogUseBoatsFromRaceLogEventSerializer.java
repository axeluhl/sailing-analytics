package com.sap.sailing.server.gateway.serialization.racelog.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogUseBoatsFromRaceLogEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceLogUseBoatsFromRaceLogEventSerializer extends BaseRaceLogEventSerializer implements
        JsonSerializer<RaceLogEvent> {
    public static final String VALUE_CLASS = RaceLogUseBoatsFromRaceLogEvent.class.getSimpleName();

    public RaceLogUseBoatsFromRaceLogEventSerializer(JsonSerializer<Competitor> competitorSerializer) {
        super(competitorSerializer);
    }

    @Override
    protected String getClassFieldValue() {
        return VALUE_CLASS;
    }

}
