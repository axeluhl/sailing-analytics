package com.sap.sailing.windestimation.data.deserializer;

import org.json.simple.JSONObject;

import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.windestimation.data.SingleDimensionBasedTwdTransition;

public class SingleDimensionBasedTwdTransitionJsonDeserializer
        implements JsonDeserializer<SingleDimensionBasedTwdTransition> {

    @Override
    public SingleDimensionBasedTwdTransition deserialize(JSONObject object) throws JsonDeserializationException {
        double dimensionValue = (double) object.get(SingleDimensionBasedTwdTransitionJsonSerializer.DIMENSION_VALUE);
        double twdChangeDegrees = (double) object.get(SingleDimensionBasedTwdTransitionJsonSerializer.TWD_CHANGE);
        SingleDimensionBasedTwdTransition twdTransition = new SingleDimensionBasedTwdTransition(dimensionValue,
                twdChangeDegrees);
        return twdTransition;
    }

}
