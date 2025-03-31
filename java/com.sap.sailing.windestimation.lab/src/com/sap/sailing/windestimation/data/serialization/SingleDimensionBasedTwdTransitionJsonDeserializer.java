package com.sap.sailing.windestimation.data.serialization;

import org.json.simple.JSONObject;

import com.sap.sailing.windestimation.data.SingleDimensionBasedTwdTransition;
import com.sap.sse.shared.json.JsonDeserializationException;
import com.sap.sse.shared.json.JsonDeserializer;

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
