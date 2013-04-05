package com.sap.sailing.server.gateway.deserialization.impl;

import java.io.Serializable;
import java.util.Collections;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;

public class RaceLogFinishPositioningListChangedEventDeserializer extends BaseRaceLogEventDeserializer {
    
    public RaceLogFinishPositioningListChangedEventDeserializer() {
    }

    @Override
    protected RaceLogEvent deserialize(JSONObject object, Serializable id, TimePoint timePoint, int passId)
            throws JsonDeserializationException {

        return factory.createFinishPositioningListChangedEvent(timePoint, id, Collections.<Competitor> emptyList(), passId);
    }

}
