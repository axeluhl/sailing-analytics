package com.sap.sailing.server.gateway.serialization.racelog.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogGateLineOpeningTimeEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceLogGateLineOpeningTimeEventSerializer extends BaseRaceLogEventSerializer {

    public static final String VALUE_CLASS = RaceLogGateLineOpeningTimeEvent.class.getSimpleName();
    public static final String FIELD_GATE_LAUNCH_STOP_TIME = "gateLineOpeningTime";     // backwards compa name
    public static final String FIELD_GATE_GOLF_DOWN_TIME = "golfDownTime";

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
        result.put(FIELD_GATE_LAUNCH_STOP_TIME, gateLineOpeningTimeEvent.getGateLineOpeningTimes().getGateLaunchStopTime());
        result.put(FIELD_GATE_GOLF_DOWN_TIME, gateLineOpeningTimeEvent.getGateLineOpeningTimes().getGolfDownTime());

        return result;
    }

}
