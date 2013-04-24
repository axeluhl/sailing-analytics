package com.sap.sailing.server.gateway.serialization.racelog.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogGateLineOpeningTimeEvent;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceLogGateLineOpeningTimeEventSerializer extends BaseRaceLogEventSerializer {

    public static final String VALUE_CLASS = RaceLogGateLineOpeningTimeEvent.class.getSimpleName();
    public static final String FIELD_GATE_LINE_OPENING_TIME = "gateLineOpeningTime";

    public RaceLogGateLineOpeningTimeEventSerializer(
            JsonSerializer<Competitor> competitorSerializer) {
        super(competitorSerializer);
    }

    @Override
    protected String getClassFieldValue() {
        return VALUE_CLASS;
    }

    @Override
    public JSONObject serialize(RaceLogEvent object) {
        RaceLogGateLineOpeningTimeEvent gateLineOpeningTimeEvent = (RaceLogGateLineOpeningTimeEvent) object;

        JSONObject result = super.serialize(gateLineOpeningTimeEvent);
        result.put(FIELD_GATE_LINE_OPENING_TIME, gateLineOpeningTimeEvent.getGateLineOpeningTime());

        return result;
    }

}
