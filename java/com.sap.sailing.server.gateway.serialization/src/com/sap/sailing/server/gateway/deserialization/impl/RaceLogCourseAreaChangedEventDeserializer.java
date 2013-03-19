package com.sap.sailing.server.gateway.deserialization.impl;

import java.io.Serializable;
import java.util.Collections;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogCourseAreaChangedEventSerializer;

public class RaceLogCourseAreaChangedEventDeserializer extends BaseRaceLogEventDeserializer {

    @Override
    protected RaceLogEvent deserialize(JSONObject object, Serializable id, TimePoint timePoint, int passId)
            throws JsonDeserializationException {

        String courseAreaId = object.get(RaceLogCourseAreaChangedEventSerializer.FIELD_COURSE_AREA_ID).toString();

        return factory.createRaceLogCourseAreaChangedEvent(timePoint, id, Collections.<Competitor> emptyList(), passId, 
                Helpers.tryUuidConversion(courseAreaId));
    }

}
