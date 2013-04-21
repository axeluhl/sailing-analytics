package com.sap.sailing.server.gateway.deserialization.racelog.impl;

import java.io.Serializable;
import java.util.List;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogGateLineOpeningTimeEventSerializer;

public class RaceLogGateLineOpeningTimeEventDeserializer extends BaseRaceLogEventDeserializer implements
        JsonDeserializer<RaceLogEvent> {

    public RaceLogGateLineOpeningTimeEventDeserializer(JsonDeserializer<Competitor> competitorDeserializer) {
        super(competitorDeserializer);
    }

    @Override
    protected RaceLogEvent deserialize(JSONObject object, Serializable id, TimePoint timePoint, int passId,
            List<Competitor> competitors) throws JsonDeserializationException {
        Long gateLineOpeningTime = (Long) object.get(RaceLogGateLineOpeningTimeEventSerializer.FIELD_GATE_LINE_OPENING_TIME);
        return factory.createGateLineOpeningTimeEvent(timePoint, id, competitors, passId, gateLineOpeningTime);
    }

}
