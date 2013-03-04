package com.sap.sailing.server.gateway.deserialization.impl;

import java.io.Serializable;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.BaseRaceLogEventSerializer;

/// TODO deserialize involved boats
public abstract class BaseRaceLogEventDeserializer implements JsonDeserializer<RaceLogEvent> {

    protected abstract RaceLogEvent deserialize(JSONObject object, Serializable id, TimePoint timePoint, int passId)
            throws JsonDeserializationException;

    @Override
    public RaceLogEvent deserialize(JSONObject object) throws JsonDeserializationException {
        // Factory handles class field and subclassing...
        String id = object.get(BaseRaceLogEventSerializer.FIELD_ID).toString();
        Number timeStamp = (Number) object.get(BaseRaceLogEventSerializer.FIELD_TIMESTAMP);
        Number passId = (Number) object.get(BaseRaceLogEventSerializer.FIELD_PASS_ID);

        return deserialize(
                object, 
                Helpers.tryUuidConversion(id), 
                new MillisecondsTimePoint(timeStamp.longValue()), 
                passId.intValue());
    }

}
