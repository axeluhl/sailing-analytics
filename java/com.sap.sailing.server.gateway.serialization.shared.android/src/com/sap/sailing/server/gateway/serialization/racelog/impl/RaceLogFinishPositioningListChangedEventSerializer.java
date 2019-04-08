package com.sap.sailing.server.gateway.serialization.racelog.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLogFinishPositioningListChangedEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceLogFinishPositioningListChangedEventSerializer extends RaceLogFinishPositioningEventSerializer {

    public static final String VALUE_CLASS = RaceLogFinishPositioningListChangedEvent.class.getSimpleName();
    
    public RaceLogFinishPositioningListChangedEventSerializer(JsonSerializer<Competitor> competitorSerializer) {
        super(competitorSerializer);
    }

    @Override
    protected String getClassFieldValue() {
        return VALUE_CLASS;
    }
}
