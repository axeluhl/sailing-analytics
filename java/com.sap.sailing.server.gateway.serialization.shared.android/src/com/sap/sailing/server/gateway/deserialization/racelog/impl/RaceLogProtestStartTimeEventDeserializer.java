package com.sap.sailing.server.gateway.deserialization.racelog.impl;

import java.io.Serializable;
import java.util.List;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogProtestStartTimeEventImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogProtestStartTimeEventSerializer;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsDurationImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class RaceLogProtestStartTimeEventDeserializer extends BaseRaceLogEventDeserializer {

    public RaceLogProtestStartTimeEventDeserializer(JsonDeserializer<Competitor> competitorDeserializer) {
        super(competitorDeserializer);
    }

    @Override
    protected RaceLogEvent deserialize(JSONObject object, Serializable id, TimePoint createdAt, AbstractLogEventAuthor author,
            TimePoint timePoint, int passId, List<Competitor> competitors) throws JsonDeserializationException {
        Long protestStartTime = (Long) object.get(RaceLogProtestStartTimeEventSerializer.FIELD_PROTEST_START_TIME);
        Long protestDuration = (Long) object.get(RaceLogProtestStartTimeEventSerializer.FIELD_PROTEST_DURATION);
        if (protestDuration == null) { // fallback for old RaceLogEvent
            protestDuration = Duration.ONE_MINUTE.times(90).asMillis();
        }
        return new RaceLogProtestStartTimeEventImpl(createdAt, timePoint, author, id,
                passId, new MillisecondsTimePoint(protestStartTime), new MillisecondsDurationImpl(protestDuration));
    }

}
