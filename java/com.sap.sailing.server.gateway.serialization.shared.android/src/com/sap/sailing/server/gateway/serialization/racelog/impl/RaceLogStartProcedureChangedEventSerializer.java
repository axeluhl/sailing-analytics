package com.sap.sailing.server.gateway.serialization.racelog.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartProcedureChangedEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceLogStartProcedureChangedEventSerializer extends BaseRaceLogEventSerializer {
    
    public static final String VALUE_CLASS = RaceLogStartProcedureChangedEvent.class.getSimpleName();
    public static final String FIELD_START_PROCEDURE_TYPE = "startProcedureType";

    public RaceLogStartProcedureChangedEventSerializer(JsonSerializer<Competitor> competitorSerializer) {
        super(competitorSerializer);
    }

    @Override
    protected String getClassFieldValue() {
        return VALUE_CLASS;
    }
    
    @Override
    public JSONObject serialize(RaceLogEvent object) {
        RaceLogStartProcedureChangedEvent event = (RaceLogStartProcedureChangedEvent) object;
        JSONObject result = super.serialize(event);
        result.put(FIELD_START_PROCEDURE_TYPE, event.getStartProcedureType().name());
        return result;
    }

}
