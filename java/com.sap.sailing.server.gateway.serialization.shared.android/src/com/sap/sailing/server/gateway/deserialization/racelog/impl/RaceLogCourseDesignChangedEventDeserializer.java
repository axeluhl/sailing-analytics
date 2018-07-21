package com.sap.sailing.server.gateway.deserialization.racelog.impl;

import java.io.Serializable;
import java.util.List;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogCourseDesignChangedEventImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.impl.DynamicCompetitor;
import com.sap.sailing.domain.common.CourseDesignerMode;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.coursedata.impl.CourseBaseDeserializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogCourseDesignChangedEventSerializer;
import com.sap.sse.common.TimePoint;

public class RaceLogCourseDesignChangedEventDeserializer extends BaseRaceLogEventDeserializer {
    
    private final CourseBaseDeserializer courseDataDeserializer;
    
    public RaceLogCourseDesignChangedEventDeserializer(JsonDeserializer<DynamicCompetitor> competitorDeserializer, CourseBaseDeserializer courseDataDeserializer) {
        super(competitorDeserializer);
        this.courseDataDeserializer = courseDataDeserializer;
    }

    @Override
    protected RaceLogEvent deserialize(JSONObject object, Serializable id, TimePoint createdAt, AbstractLogEventAuthor author, TimePoint timePoint, int passId, List<Competitor> competitors)
            throws JsonDeserializationException {
        JSONObject jsonCourseDesign = (JSONObject) object.get(RaceLogCourseDesignChangedEventSerializer.FIELD_COURSE_DESIGN);
        CourseBase courseData = courseDataDeserializer.deserialize(jsonCourseDesign);
        final String courseDesignerModeName = (String) object.get(RaceLogCourseDesignChangedEventSerializer.FIELD_COURSE_DESIGNER_MODE);
        return new RaceLogCourseDesignChangedEventImpl(createdAt, timePoint, author, id, passId, courseData,
                courseDesignerModeName == null ? null : CourseDesignerMode.valueOf(courseDesignerModeName));
    }
}
