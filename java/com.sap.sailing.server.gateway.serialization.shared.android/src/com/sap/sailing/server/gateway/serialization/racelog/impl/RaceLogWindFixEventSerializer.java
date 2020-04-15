package com.sap.sailing.server.gateway.serialization.racelog.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogWindFixEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceLogWindFixEventSerializer extends BaseRaceLogEventSerializer {
    
    public static final String VALUE_CLASS = RaceLogWindFixEvent.class.getSimpleName();
    public static final String FIELD_WIND = "wind";
    public static final String FIELD_MAGNETIC = "magnetic";
    
    JsonSerializer<Wind> windSerializer;

    public RaceLogWindFixEventSerializer(JsonSerializer<Competitor> competitorSerializer, JsonSerializer<Wind> windSerializer) {
        super(competitorSerializer);
        this.windSerializer = windSerializer;
    }

    @Override
    protected String getClassFieldValue() {
        return VALUE_CLASS;
    }
    
    @Override
    public JSONObject serialize(RaceLogEvent object) {
        RaceLogWindFixEvent event = (RaceLogWindFixEvent) object;
        JSONObject result = super.serialize(event);
        result.put(FIELD_WIND, windSerializer.serialize(event.getWindFix()));
        result.put(FIELD_MAGNETIC, event.isMagnetic());
        return result;
    }

}
