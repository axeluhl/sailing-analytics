package com.sap.sailing.server.gateway.deserialization.racelog.impl;

import java.io.Serializable;
import java.util.List;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogCourseAreaChangedEventSerializer;
import com.sap.sse.common.TimePoint;

public class RaceLogCourseAreaChangedEventDeserializer extends BaseRaceLogEventDeserializer {
    
    public RaceLogCourseAreaChangedEventDeserializer(JsonDeserializer<Competitor> competitorDeserializer) {
        super(competitorDeserializer);
    }

    @Override
    protected RaceLogEvent deserialize(JSONObject object, Serializable id, TimePoint createdAt, RaceLogEventAuthor author, TimePoint timePoint, int passId, List<Competitor> competitors)
            throws JsonDeserializationException {
        Serializable courseAreaId = (Serializable) object.get(RaceLogCourseAreaChangedEventSerializer.FIELD_COURSE_AREA_ID);
        return factory.createCourseAreaChangedEvent(createdAt, author, timePoint, id, competitors, 
                passId, Helpers.tryUuidConversion(courseAreaId));
    }

}
