package com.sap.sailing.server.gateway.deserialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.SpeedWithConfidence;
import com.sap.sailing.domain.base.impl.SpeedWithConfidenceImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.SpeedWithConfidenceWithIntegerRelationJsonSerializer;
import com.sap.sse.common.Speed;

public class SpeedWithConfidenceWithIntegerRelationJsonDeserializer implements
        JsonDeserializer<SpeedWithConfidence<Integer>> {

    @Override
    public SpeedWithConfidence<Integer> deserialize(JSONObject object) throws JsonDeserializationException {
        double speedInKnots = (Double) object.get(SpeedWithConfidenceWithIntegerRelationJsonSerializer.FIELD_SPEED);
        double confidence = (Double) object.get(SpeedWithConfidenceWithIntegerRelationJsonSerializer.FIELD_CONFIDENCE);
        int relativeToValue = (Integer) object
                .get(SpeedWithConfidenceWithIntegerRelationJsonSerializer.FIELD_RELATION_INT);
        Speed speed = new KnotSpeedImpl(speedInKnots);
        return new SpeedWithConfidenceImpl<Integer>(speed, confidence, relativeToValue);
    }

}
