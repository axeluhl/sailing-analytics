package com.sap.sailing.server.gateway.serialization.racelog.impl;

import com.sap.sailing.domain.abstractlog.shared.events.RegisterCompetitorAndBoatEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceLogRegisterCompetitorAndBoatEventSerializer extends BaseRaceLogEventSerializer {

    public static final String VALUE_CLASS = RegisterCompetitorAndBoatEvent.class.getSimpleName();

    public RaceLogRegisterCompetitorAndBoatEventSerializer(
            JsonSerializer<Competitor> competitorSerializer) {
        super(competitorSerializer);
    }

    @Override
    protected String getClassFieldValue() {
        return VALUE_CLASS;
    }
}
