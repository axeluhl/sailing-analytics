package com.sap.sailing.server.gateway.serialization.racelog.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.race.RaceLogCourseDesignChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceLogCourseDesignChangedEventSerializer extends BaseRaceLogEventSerializer {

    public static final String VALUE_CLASS = RaceLogCourseDesignChangedEvent.class.getSimpleName();
    public static final String FIELD_COURSE_DESIGN = "courseDesign";
    public static final String FIELD_COURSE_DESIGNER_MODE = "courseDesignerMode";
    
    private final JsonSerializer<CourseBase> courseBaseSerializer;

    public RaceLogCourseDesignChangedEventSerializer(
            JsonSerializer<Competitor> competitorSerializer,
            JsonSerializer<CourseBase> courseBaseSerializer) {
        super(competitorSerializer);
        this.courseBaseSerializer = courseBaseSerializer;
    }

    @Override
    protected String getClassFieldValue() {
        return VALUE_CLASS;
    }

    @Override
    public JSONObject serialize(RaceLogEvent object) {
        RaceLogCourseDesignChangedEvent courseChangedEvent = (RaceLogCourseDesignChangedEvent) object;
        JSONObject result = super.serialize(courseChangedEvent);
        result.put(FIELD_COURSE_DESIGN, courseBaseSerializer.serialize(courseChangedEvent.getCourseDesign()));
        result.put(FIELD_COURSE_DESIGNER_MODE, courseChangedEvent.getCourseDesignerMode() == null ? null : courseChangedEvent.getCourseDesignerMode().name());
        return result;
    }

}
