package com.sap.sailing.server.gateway.serialization.racelog.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartTimeEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceLogStartTimeEventSerializer extends RaceLogRaceStatusEventSerializer {

    public static final String VALUE_CLASS = RaceLogStartTimeEvent.class.getSimpleName();
    public static final String FIELD_START_TIME = "startTime";
    public static final String FIELD_COURSE_AREA_ID_AS_STRING = "courseAreaIdAsString";

    public RaceLogStartTimeEventSerializer(JsonSerializer<Competitor> competitorSerializer) {
        super(competitorSerializer);
    }

    @Override
    protected String getClassFieldValue() {
        return VALUE_CLASS;
    }

    @Override
    public JSONObject serialize(RaceLogEvent object) {
        RaceLogStartTimeEvent startTimeEvent = (RaceLogStartTimeEvent) object;
        JSONObject result = super.serialize(startTimeEvent);
        result.put(FIELD_START_TIME, startTimeEvent.getStartTime().asMillis());
        result.put(FIELD_COURSE_AREA_ID_AS_STRING, startTimeEvent.getCourseAreaId()==null?null:
            startTimeEvent.getCourseAreaId().toString());
        return result;
    }
}
