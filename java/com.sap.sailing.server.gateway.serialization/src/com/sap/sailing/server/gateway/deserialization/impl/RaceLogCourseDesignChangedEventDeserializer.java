package com.sap.sailing.server.gateway.deserialization.impl;

import java.io.Serializable;
import java.util.Collections;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseData;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.coursedata.impl.CourseDataDeserializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogCourseDesignChangedEventSerializer;

public class RaceLogCourseDesignChangedEventDeserializer extends BaseRaceLogEventDeserializer {
    
    private final CourseDataDeserializer courseDataDeserializer;
    
    public RaceLogCourseDesignChangedEventDeserializer(CourseDataDeserializer courseDataDeserializer) {
        this.courseDataDeserializer = courseDataDeserializer;
    }

    @Override
    protected RaceLogEvent deserialize(JSONObject object, Serializable id, TimePoint timePoint, int passId)
            throws JsonDeserializationException {

        JSONObject jsonCourseDesign = (JSONObject) object.get(RaceLogCourseDesignChangedEventSerializer.FIELD_COURSE_DESIGN);
        CourseData courseData = courseDataDeserializer.deserialize(jsonCourseDesign);

        return factory.createCourseDesignChangedEvent(timePoint, id, Collections.<Competitor> emptyList(), passId, courseData);
    }

}
