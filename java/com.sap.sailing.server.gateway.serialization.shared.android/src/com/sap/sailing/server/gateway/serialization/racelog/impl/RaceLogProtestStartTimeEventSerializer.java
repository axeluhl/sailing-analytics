package com.sap.sailing.server.gateway.serialization.racelog.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogProtestStartTimeEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceLogProtestStartTimeEventSerializer extends BaseRaceLogEventSerializer {
    
    public static final String VALUE_CLASS = RaceLogProtestStartTimeEvent.class.getSimpleName();
    public static final String FIELD_PROTEST_START_TIME = "protestStartTime";
    public static final String FIELD_PROTEST_END_TIME = "protestEndTime";

    public RaceLogProtestStartTimeEventSerializer(JsonSerializer<Competitor> competitorSerializer) {
        super(competitorSerializer);
    }

    @Override
    protected String getClassFieldValue() {
        return VALUE_CLASS;
    }
    
    @Override
    public JSONObject serialize(RaceLogEvent object) {
        RaceLogProtestStartTimeEvent event = (RaceLogProtestStartTimeEvent) object;
        JSONObject result = super.serialize(event);
        result.put(FIELD_PROTEST_START_TIME, event.getProtestTime().from().asMillis());
        result.put(FIELD_PROTEST_END_TIME, event.getProtestTime().to().asMillis());
        return result;
    }

}
