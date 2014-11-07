package com.sap.sailing.server.gateway.deserialization.racelog.impl;

import java.io.Serializable;
import java.util.List;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventAuthor;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogDefineMarkEventSerializer;

public class RaceLogDefineMarkEventDeserializer extends BaseRaceLogEventDeserializer {	
    private final JsonDeserializer<Mark> markDeserializer;
    public RaceLogDefineMarkEventDeserializer(JsonDeserializer<Competitor> competitorDeserializer,
            JsonDeserializer<Mark> markDeserializer) {
        super(competitorDeserializer);
        this.markDeserializer = markDeserializer;
    }

    @Override
    protected RaceLogEvent deserialize(JSONObject object, Serializable id, TimePoint createdAt, RaceLogEventAuthor author, TimePoint timePoint, int passId, List<Competitor> competitors)
            throws JsonDeserializationException {
    	Mark mark = markDeserializer.deserialize(Helpers.getNestedObjectSafe(object, RaceLogDefineMarkEventSerializer.FIELD_MARK));
        
        return factory.createDefineMarkEvent(createdAt, author, timePoint, id, passId, mark);
    }
}
