package com.sap.sailing.server.gateway.serialization.racelog.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.race.FixedMarkPassingEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceLogFixedMarkPassingEventSerializer extends BaseRaceLogEventSerializer implements JsonSerializer<RaceLogEvent> {

    public RaceLogFixedMarkPassingEventSerializer(JsonSerializer<Competitor> competitorSerializer) {
        super(competitorSerializer);
    }

    public static final String VALUE_CLASS = FixedMarkPassingEvent.class.getSimpleName();

    public static final String FIELD_TIMEPOINT_OF_MARKPASSING = "timePointOfMarkPassing";
    public static final String FIELD_INDEX_OF_PASSED_WAYPOINT = "indexOfPassedWaypoint";
    
    @Override
    protected String getClassFieldValue() {
        return VALUE_CLASS;
    }
    
    @Override
    public JSONObject serialize(RaceLogEvent object) {
        FixedMarkPassingEvent event = (FixedMarkPassingEvent) object;
        
        JSONObject result = super.serialize(event);
        result.put(FIELD_INDEX_OF_PASSED_WAYPOINT, event.getZeroBasedIndexOfPassedWaypoint());
        result.put(FIELD_TIMEPOINT_OF_MARKPASSING, event.getTimePointOfFixedPassing().asMillis());
        return result;
    }

}
