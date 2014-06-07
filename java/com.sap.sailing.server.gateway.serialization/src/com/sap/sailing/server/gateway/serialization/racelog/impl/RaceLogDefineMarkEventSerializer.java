package com.sap.sailing.server.gateway.serialization.racelog.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.tracking.DefineMarkEvent;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceLogDefineMarkEventSerializer extends BaseRaceLogEventSerializer {
    public static final String VALUE_CLASS = DefineMarkEvent.class.getSimpleName();
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
        DefineMarkEvent event = (DefineMarkEvent) object;

        JSONObject result = super.serialize(event);
        result.put(FIELD_MARK, markSerializer.serialize(event.getMark()));
        
        return result;
    }

}
