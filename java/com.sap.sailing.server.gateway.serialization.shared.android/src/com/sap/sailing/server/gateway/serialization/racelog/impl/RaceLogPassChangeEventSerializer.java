package com.sap.sailing.server.gateway.serialization.racelog.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogPassChangeEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceLogPassChangeEventSerializer extends BaseRaceLogEventSerializer {

    public static final String VALUE_CLASS = RaceLogPassChangeEvent.class.getSimpleName();
    
    public RaceLogPassChangeEventSerializer(JsonSerializer<Competitor> competitorSerializer) {
        super(competitorSerializer);
    }

    @Override
    protected String getClassFieldValue() {
        return VALUE_CLASS;
    }
    
    @Override
    public JSONObject serialize(RaceLogEvent object) {
        RaceLogPassChangeEvent event = (RaceLogPassChangeEvent) object;    
        return super.serialize(event);
    }


}
