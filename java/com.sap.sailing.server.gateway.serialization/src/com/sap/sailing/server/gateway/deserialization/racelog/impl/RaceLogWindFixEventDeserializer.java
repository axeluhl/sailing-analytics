package com.sap.sailing.server.gateway.deserialization.racelog.impl;

import java.io.Serializable;
import java.util.List;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventAuthor;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogWindFixEventSerializer;

public class RaceLogWindFixEventDeserializer extends BaseRaceLogEventDeserializer {
    
    private final JsonDeserializer<Wind> windDeserializer;

    public RaceLogWindFixEventDeserializer(JsonDeserializer<Competitor> competitorDeserializer, JsonDeserializer<Wind> windDeserializer) {
        super(competitorDeserializer);
        this.windDeserializer = windDeserializer;
    }

    @Override
    protected RaceLogEvent deserialize(JSONObject object, Serializable id, TimePoint createdAt, RaceLogEventAuthor author,
            TimePoint timePoint, int passId, List<Competitor> competitors) throws JsonDeserializationException {
        JSONObject windJsonObject = Helpers.getNestedObjectSafe(object, RaceLogWindFixEventSerializer.FIELD_WIND);
        
        Wind wind = windDeserializer.deserialize(windJsonObject);
        
        return factory.createWindFixEvent(createdAt, author, timePoint, id, competitors, passId, wind);
    }

}
