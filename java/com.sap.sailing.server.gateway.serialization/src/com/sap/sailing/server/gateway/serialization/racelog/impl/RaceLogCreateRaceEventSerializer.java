package com.sap.sailing.server.gateway.serialization.racelog.impl;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.racelog.tracking.CreateRaceEvent;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceLogCreateRaceEventSerializer extends BaseRaceLogEventSerializer {
    public static final String VALUE_CLASS = CreateRaceEvent.class.getSimpleName();

    public RaceLogCreateRaceEventSerializer(
            JsonSerializer<Competitor> competitorSerializer) {
        super(competitorSerializer);
    }

    @Override
    protected String getClassFieldValue() {
        return VALUE_CLASS;
    }

}
