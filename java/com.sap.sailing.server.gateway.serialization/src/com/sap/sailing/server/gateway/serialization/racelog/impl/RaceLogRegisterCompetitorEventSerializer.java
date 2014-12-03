package com.sap.sailing.server.gateway.serialization.racelog.impl;

import com.sap.sailing.domain.abstractlog.race.tracking.RegisterCompetitorEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceLogRegisterCompetitorEventSerializer extends BaseRaceLogEventSerializer {

    public static final String VALUE_CLASS = RegisterCompetitorEvent.class.getSimpleName();

    public RaceLogRegisterCompetitorEventSerializer(
            JsonSerializer<Competitor> competitorSerializer) {
        super(competitorSerializer);
    }

    @Override
    protected String getClassFieldValue() {
        return VALUE_CLASS;
    }
}
