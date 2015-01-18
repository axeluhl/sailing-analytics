package com.sap.sailing.server.gateway.deserialization.racelog.impl;

import java.io.Serializable;
import java.util.List;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogSuppressedMarkPassingsEventSerializer;
import com.sap.sse.common.TimePoint;

public class RaceLogSuppressedMarkPassingsEventDeserializer extends BaseRaceLogEventDeserializer implements JsonDeserializer<RaceLogEvent> {

    public RaceLogSuppressedMarkPassingsEventDeserializer(JsonDeserializer<Competitor> competitorDeserializer) {
        super(competitorDeserializer);
    }

    @Override
    protected RaceLogEvent deserialize(JSONObject object, Serializable id, TimePoint createdAt, AbstractLogEventAuthor author,
            TimePoint timePoint, int passId, List<Competitor> competitors) throws JsonDeserializationException {
        Integer indexOfFirstWaypoint = (Integer) object.get(RaceLogSuppressedMarkPassingsEventSerializer.FIELD_INDEX_OF_FIRST_SUPPRESSED_WAYPOINTS);
        return factory.createSuppressedMarkPassingsEvent(timePoint, author, id, competitors, passId, indexOfFirstWaypoint);
    }
}
