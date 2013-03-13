package com.sap.sailing.server.gateway.serialization.racelog.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseData;
import com.sap.sailing.domain.racelog.RaceLogCourseDesignChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceLogCourseDesignChangedEventSerializer extends BaseRaceLogEventSerializer {

    public static final String VALUE_CLASS = RaceLogCourseDesignChangedEvent.class.getSimpleName();
    public static final String FIELD_COURSE_DESIGN = "courseDesign";
    
    private final JsonSerializer<CourseData> courseDataSerializer;

    public RaceLogCourseDesignChangedEventSerializer(
            JsonSerializer<Competitor> competitorSerializer,
            JsonSerializer<CourseData> courseDataSerializer) {
        super(competitorSerializer);
        this.courseDataSerializer = courseDataSerializer;
    }

    @Override
    protected String getClassFieldValue() {
        return VALUE_CLASS;
    }

    @Override
    public JSONObject serialize(RaceLogEvent object) {
        RaceLogCourseDesignChangedEvent courseChangedEvent = (RaceLogCourseDesignChangedEvent) object;

        JSONObject result = super.serialize(courseChangedEvent);
        result.put(FIELD_COURSE_DESIGN, courseDataSerializer.serialize(courseChangedEvent.getCourseDesign()));

        return result;
    }

}
