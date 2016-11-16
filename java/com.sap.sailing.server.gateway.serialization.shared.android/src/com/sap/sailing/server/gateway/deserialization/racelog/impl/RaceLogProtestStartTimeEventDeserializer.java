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
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.common.impl.TimeRangeImpl;

public class RaceLogProtestStartTimeEventDeserializer extends BaseRaceLogEventDeserializer {

    public RaceLogProtestStartTimeEventDeserializer(JsonDeserializer<Competitor> competitorDeserializer) {
        super(competitorDeserializer);
    }

    @Override
    protected RaceLogEvent deserialize(JSONObject object, Serializable id, TimePoint createdAt, AbstractLogEventAuthor author,
            TimePoint timePoint, int passId, List<Competitor> competitors) throws JsonDeserializationException {
        Long protestStart = (Long) object.get(RaceLogProtestStartTimeEventSerializer.FIELD_PROTEST_START_TIME);
        Long protestEnd = (Long) object.get(RaceLogProtestStartTimeEventSerializer.FIELD_PROTEST_END_TIME);
        TimeRange protestTime = new TimeRangeImpl(new MillisecondsTimePoint(protestStart), new MillisecondsTimePoint(protestEnd));
        return new RaceLogProtestStartTimeEventImpl(createdAt, timePoint, author, id, passId, protestTime);
    }

}
