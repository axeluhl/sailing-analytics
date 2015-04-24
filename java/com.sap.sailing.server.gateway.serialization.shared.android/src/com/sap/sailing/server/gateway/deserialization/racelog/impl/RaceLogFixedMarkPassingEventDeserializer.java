package com.sap.sailing.server.gateway.deserialization.racelog.impl;

import java.io.Serializable;
import java.util.List;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogFixedMarkPassingEventSerializer;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class RaceLogFixedMarkPassingEventDeserializer extends BaseRaceLogEventDeserializer implements JsonDeserializer<RaceLogEvent> {

    public RaceLogFixedMarkPassingEventDeserializer(JsonDeserializer<Competitor> competitorDeserializer) {
        super(competitorDeserializer);
    }

    @Override
    protected RaceLogEvent deserialize(JSONObject object, Serializable id, TimePoint createdAt, AbstractLogEventAuthor author,
            TimePoint timePoint, int passId, List<Competitor> competitors) throws JsonDeserializationException {
        TimePoint ofPassing = new MillisecondsTimePoint(
                (Long) object.get(RaceLogFixedMarkPassingEventSerializer.FIELD_TIMEPOINT_OF_MARKPASSING));
        Integer zeroBasedIndexOfPassedWaypoint = (Integer) object
                .get(RaceLogFixedMarkPassingEventSerializer.FIELD_INDEX_OF_PASSED_WAYPOINT);
        return factory.createFixedMarkPassingEvent(timePoint, author, id, competitors, passId, ofPassing,
                zeroBasedIndexOfPassedWaypoint);
    }
}
