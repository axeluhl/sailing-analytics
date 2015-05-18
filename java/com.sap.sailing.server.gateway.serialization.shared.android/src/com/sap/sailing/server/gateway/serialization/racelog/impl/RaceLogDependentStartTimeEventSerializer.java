package com.sap.sailing.server.gateway.serialization.racelog.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.race.RaceLogDependentStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceLogDependentStartTimeEventSerializer extends BaseRaceLogEventSerializer implements JsonSerializer<RaceLogEvent> {

    public RaceLogDependentStartTimeEventSerializer(JsonSerializer<Competitor> competitorSerializer) {
        super(competitorSerializer);
    }

    public static final String VALUE_CLASS = RaceLogDependentStartTimeEvent.class.getSimpleName();

    public static final String FIELD_DEPDENDENT_ON_FLEET = "dependentOnFleet";
    public static final String FIELD_START_TIME_DIFFERENCE = "startTimeDifference";
    
    @Override
    protected String getClassFieldValue() {
        return VALUE_CLASS;
    }
    
    @Override
    public JSONObject serialize(RaceLogEvent object) {
        RaceLogDependentStartTimeEvent event = (RaceLogDependentStartTimeEvent) object;
        
        JSONObject result = super.serialize(event);
        result.put(FIELD_DEPDENDENT_ON_FLEET, event.getDependentOnFleet());
        result.put(FIELD_START_TIME_DIFFERENCE, event.getStartTimeDifference().asMillis());
        return result;
    }

}
