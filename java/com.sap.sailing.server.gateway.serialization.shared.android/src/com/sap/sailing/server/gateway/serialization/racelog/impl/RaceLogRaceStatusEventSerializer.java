package com.sap.sailing.server.gateway.serialization.racelog.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogRaceStatusEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceLogRaceStatusEventSerializer extends BaseRaceLogEventSerializer {

    public static final String VALUE_CLASS = RaceLogRaceStatusEvent.class.getSimpleName();
    public static final String FIELD_NEXT_STATUS = "nextStatus";

    public RaceLogRaceStatusEventSerializer(JsonSerializer<Competitor> competitorSerializer) {
        super(competitorSerializer);
    }

    @Override
    protected String getClassFieldValue() {
        return VALUE_CLASS;
    }

    @Override
    public JSONObject serialize(RaceLogEvent object) {
        RaceLogRaceStatusEvent event = (RaceLogRaceStatusEvent) object;
        JSONObject result = super.serialize(event);
        result.put(FIELD_NEXT_STATUS, event.getNextStatus().toString());
        return result;
    }

}
