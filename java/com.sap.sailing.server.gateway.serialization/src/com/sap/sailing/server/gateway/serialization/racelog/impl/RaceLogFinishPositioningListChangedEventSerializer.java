package com.sap.sailing.server.gateway.serialization.racelog.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogFinishPositioningListChangedEvent;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceLogFinishPositioningListChangedEventSerializer extends BaseRaceLogEventSerializer {

    public static final String VALUE_CLASS = RaceLogFinishPositioningListChangedEvent.class.getSimpleName();
    
    public RaceLogFinishPositioningListChangedEventSerializer(JsonSerializer<Competitor> competitorSerializer) {
        super(competitorSerializer);
    }

    @Override
    protected String getClassFieldValue() {
        return VALUE_CLASS;
    }

    @Override
    public JSONObject serialize(RaceLogEvent object) {
        RaceLogFinishPositioningListChangedEvent finishPositioningListEvent = (RaceLogFinishPositioningListChangedEvent) object;

        JSONObject result = super.serialize(finishPositioningListEvent);

        return result;
    }

}
