package com.sap.sailing.server.gateway.serialization.racelog.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLogFinishPositioningConfirmedEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceLogFinishPositioningConfirmedEventSerializer extends RaceLogFinishPositioningEventSerializer {

    public static final String VALUE_CLASS = RaceLogFinishPositioningConfirmedEvent.class.getSimpleName();
    
    public RaceLogFinishPositioningConfirmedEventSerializer(JsonSerializer<Competitor> competitorSerializer) {
        super(competitorSerializer);
    }

    @Override
    protected String getClassFieldValue() {
        return VALUE_CLASS;
    }
}
