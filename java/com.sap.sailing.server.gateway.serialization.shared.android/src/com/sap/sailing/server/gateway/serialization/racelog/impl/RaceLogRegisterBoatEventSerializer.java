package com.sap.sailing.server.gateway.serialization.racelog.impl;

import com.sap.sailing.domain.abstractlog.shared.events.RegisterBoatEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceLogRegisterBoatEventSerializer extends BaseRaceLogEventSerializer {

    public static final String VALUE_CLASS = RegisterBoatEvent.class.getSimpleName();

    public RaceLogRegisterBoatEventSerializer(
            JsonSerializer<Competitor> competitorSerializer) {
        super(competitorSerializer);
    }

    @Override
    protected String getClassFieldValue() {
        return VALUE_CLASS;
    }
}
