package com.sap.sailing.server.gateway.deserialization.racelog.impl;

import java.io.Serializable;
import java.util.List;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogGateLineOpeningTimeEventSerializer;

public class RaceLogGateLineOpeningTimeEventDeserializer extends BaseRaceLogEventDeserializer implements
        JsonDeserializer<RaceLogEvent> {

    public RaceLogGateLineOpeningTimeEventDeserializer(JsonDeserializer<Competitor> competitorDeserializer) {
        super(competitorDeserializer);
    }

    @Override
    protected RaceLogEvent deserialize(JSONObject object, Serializable id, TimePoint createdAt, AbstractLogEventAuthor author, TimePoint timePoint,
            int passId, List<Competitor> competitors) throws JsonDeserializationException {
        Number gateLaunchTime = (Number) object.get(RaceLogGateLineOpeningTimeEventSerializer.FIELD_GATE_LAUNCH_STOP_TIME);
        Number golfDownTime = 0;
        if (object.containsKey(RaceLogGateLineOpeningTimeEventSerializer.FIELD_GATE_GOLF_DOWN_TIME)) {
            golfDownTime = (Number) object.get(RaceLogGateLineOpeningTimeEventSerializer.FIELD_GATE_GOLF_DOWN_TIME);
        }
        return factory.createGateLineOpeningTimeEvent(timePoint, author, id, competitors, passId, gateLaunchTime.longValue(), golfDownTime.longValue());
    }
}
