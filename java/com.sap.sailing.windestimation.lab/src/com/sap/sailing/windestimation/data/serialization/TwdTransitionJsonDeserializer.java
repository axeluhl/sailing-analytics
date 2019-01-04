package com.sap.sailing.windestimation.data.serialization;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.windestimation.data.LabelledTwdTransition;
import com.sap.sailing.windestimation.data.ManeuverTypeForClassification;
import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sse.common.impl.DegreeBearingImpl;
import com.sap.sse.common.impl.MillisecondsDurationImpl;

public class TwdTransitionJsonDeserializer implements JsonDeserializer<TwdTransition> {

    @Override
    public TwdTransition deserialize(JSONObject object) throws JsonDeserializationException {
        long durationMillis = (long) object.get(TwdTransitionJsonSerializer.DURATION);
        double distanceMeters = (double) object.get(TwdTransitionJsonSerializer.DISTANCE);
        double twdChangeDegrees = (double) object.get(TwdTransitionJsonSerializer.TWD_CHANGE);
        ManeuverTypeForClassification fromManeuverType = ManeuverTypeForClassification
                .valueOf((String) object.get(TwdTransitionJsonSerializer.FROM_MANEUVER_TYPE));
        ManeuverTypeForClassification toManeuverType = ManeuverTypeForClassification
                .valueOf((String) object.get(TwdTransitionJsonSerializer.TO_MANEUVER_TYPE));
        TwdTransition twdTransition = new TwdTransition(new MeterDistance(distanceMeters),
                new MillisecondsDurationImpl(durationMillis), new DegreeBearingImpl(twdChangeDegrees), fromManeuverType,
                toManeuverType);
        if (object.containsKey(TwdTransitionJsonSerializer.CORRECT)) {
            boolean correct = (boolean) object.get(TwdTransitionJsonSerializer.CORRECT);
            boolean testDataset = (boolean) object.get(TwdTransitionJsonSerializer.TEST_DATASET);
            twdTransition = new LabelledTwdTransition(twdTransition.getDistance(), twdTransition.getDuration(),
                    twdTransition.getTwdChange(), correct, fromManeuverType, toManeuverType, testDataset);
        }
        return twdTransition;
    }

}
