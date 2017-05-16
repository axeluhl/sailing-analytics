package com.sap.sailing.server.gateway.serialization.racelog.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.race.RaceLogCourseAreaChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceLogCourseAreaChangedEventSerializer extends BaseRaceLogEventSerializer {

    public static final String VALUE_CLASS = RaceLogCourseAreaChangedEvent.class.getSimpleName();
    public static final String FIELD_COURSE_AREA_ID = "courseAreaId";

    public RaceLogCourseAreaChangedEventSerializer(
            JsonSerializer<Competitor> competitorSerializer) {
        super(competitorSerializer);
    }

    @Override
    protected String getClassFieldValue() {
        return VALUE_CLASS;
    }

    @Override
    public JSONObject serialize(RaceLogEvent object) {
        RaceLogCourseAreaChangedEvent caChangedEvent = (RaceLogCourseAreaChangedEvent) object;

        JSONObject result = super.serialize(caChangedEvent);
        result.put(FIELD_COURSE_AREA_ID, caChangedEvent.getCourseAreaId().toString());

        return result;
    }

}
