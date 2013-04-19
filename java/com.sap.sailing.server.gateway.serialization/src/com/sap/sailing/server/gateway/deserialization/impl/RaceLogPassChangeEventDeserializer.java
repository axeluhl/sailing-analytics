package com.sap.sailing.server.gateway.deserialization.impl;

import java.io.Serializable;
import java.util.List;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;

public class RaceLogPassChangeEventDeserializer extends BaseRaceLogEventDeserializer {
    
    public RaceLogPassChangeEventDeserializer(JsonDeserializer<Competitor> competitorDeserializer) {
        super(competitorDeserializer);
    }

    @Override
    protected RaceLogEvent deserialize(JSONObject object, Serializable id, TimePoint timePoint, int passId, List<Competitor> competitors) {

        return factory.createPassChangeEvent(timePoint, id, competitors, passId);
    }

}
