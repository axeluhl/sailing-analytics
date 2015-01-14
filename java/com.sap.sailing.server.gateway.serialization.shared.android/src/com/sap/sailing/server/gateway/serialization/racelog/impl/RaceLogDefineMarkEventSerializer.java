package com.sap.sailing.server.gateway.serialization.racelog.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogDefineMarkEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceLogDefineMarkEventSerializer extends BaseRaceLogEventSerializer {
    public static final String VALUE_CLASS = RaceLogDefineMarkEvent.class.getSimpleName();
    public static final String FIELD_MARK = "mark";
    
    private final JsonSerializer<ControlPoint> markSerializer;

    public RaceLogDefineMarkEventSerializer(
            JsonSerializer<Competitor> competitorSerializer, JsonSerializer<ControlPoint> markSerializer) {
        super(competitorSerializer);
        this.markSerializer = markSerializer;
    }

    @Override
    protected String getClassFieldValue() {
        return VALUE_CLASS;
    }

    @Override
    public JSONObject serialize(RaceLogEvent object) {
        RaceLogDefineMarkEvent event = (RaceLogDefineMarkEvent) object;

        JSONObject result = super.serialize(event);
        result.put(FIELD_MARK, markSerializer.serialize(event.getMark()));
        
        return result;
    }

}
