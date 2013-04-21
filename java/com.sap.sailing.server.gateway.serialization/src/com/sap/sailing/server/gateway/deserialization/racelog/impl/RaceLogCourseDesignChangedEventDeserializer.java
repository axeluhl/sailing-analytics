package com.sap.sailing.server.gateway.deserialization.racelog.impl;

import java.io.Serializable;
import java.util.List;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.coursedata.impl.CourseDataDeserializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogCourseDesignChangedEventSerializer;

public class RaceLogCourseDesignChangedEventDeserializer extends BaseRaceLogEventDeserializer {
    
    private final CourseDataDeserializer courseDataDeserializer;
    
    public RaceLogCourseDesignChangedEventDeserializer(JsonDeserializer<Competitor> competitorDeserializer, CourseDataDeserializer courseDataDeserializer) {
        super(competitorDeserializer);
        this.courseDataDeserializer = courseDataDeserializer;
    }

    @Override
    protected RaceLogEvent deserialize(JSONObject object, Serializable id, TimePoint createdAt, TimePoint timePoint, int passId, List<Competitor> competitors)
            throws JsonDeserializationException {

        JSONObject jsonCourseDesign = (JSONObject) object.get(RaceLogCourseDesignChangedEventSerializer.FIELD_COURSE_DESIGN);
        CourseBase courseData = courseDataDeserializer.deserialize(jsonCourseDesign);

        return factory.createCourseDesignChangedEvent(createdAt, timePoint, id, competitors, passId, courseData);
    }

}
