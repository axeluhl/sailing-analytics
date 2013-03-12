package com.sap.sailing.server.gateway.deserialization.impl;

import java.io.Serializable;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogRaceStatusEvent;
import com.sap.sailing.domain.racelog.impl.RaceLogStartTimeEventImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.serialization.impl.racelog.RaceLogStartTimeEventSerializer;

public class RaceLogStartTimeEventDeserializer extends RaceLogRaceStatusEventDeserializer {

    @Override
    protected RaceLogEvent deserialize(JSONObject object, Serializable id, TimePoint timePoint, int passId)
            throws JsonDeserializationException {

        long startTime = (Long) object.get(RaceLogStartTimeEventSerializer.FIELD_START_TIME);

        RaceLogRaceStatusEvent event = (RaceLogRaceStatusEvent) super.deserialize(object, id, timePoint, passId);

        return new RaceLogStartTimeEventImpl(event.getTimePoint(), event.getId(), event.getInvolvedBoats(),
                event.getPassId(), event.getNextStatus(), new MillisecondsTimePoint(startTime));
    }

}
