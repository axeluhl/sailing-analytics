package com.sap.sailing.server.gateway.deserialization.racelog.impl;

import java.io.Serializable;
import java.util.List;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sse.common.TimePoint;

public class RaceLogRegisterCompetitorEventDeserializer extends BaseRaceLogEventDeserializer {
    
    public RaceLogRegisterCompetitorEventDeserializer(JsonDeserializer<Competitor> competitorDeserializer) {
        super(competitorDeserializer);
    }

    @Override
    protected RaceLogEvent deserialize(JSONObject object, Serializable id, TimePoint createdAt, RaceLogEventAuthor author, TimePoint timePoint, int passId, List<Competitor> competitors) {
        assert competitors.size() == 1 : "Expected exactly one competitor for RegisterCompetitorEvent";
    	return factory.createRegisterCompetitorEvent(createdAt, author, timePoint, id, passId, competitors.get(0));
    }

}
