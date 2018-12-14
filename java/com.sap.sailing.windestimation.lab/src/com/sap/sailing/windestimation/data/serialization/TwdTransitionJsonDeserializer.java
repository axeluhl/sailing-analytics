package com.sap.sailing.windestimation.data.serialization;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.windestimation.data.LabelledTwdTransition;
import com.sap.sailing.windestimation.data.ManeuverTypeForClassification;
import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sse.common.impl.DegreeBearingImpl;
import com.sap.sse.common.impl.MillisecondsDurationImpl;

public class TwdTransitionJsonDeserializer implements JsonDeserializer<TwdTransition> {

    private final DomainFactory domainFactory;

    public TwdTransitionJsonDeserializer(DomainFactory domainFactory) {
        this.domainFactory = domainFactory;
    }

    @Override
    public TwdTransition deserialize(JSONObject object) throws JsonDeserializationException {
        long durationMillis = (long) object.get(TwdTransitionJsonSerializer.DURATION);
        double distanceMeters = (double) object.get(TwdTransitionJsonSerializer.DISTANCE);
        String boatClassName = (String) object.get(TwdTransitionJsonSerializer.BOAT_CLASS);
        double twdChangeDegrees = (double) object.get(TwdTransitionJsonSerializer.TWD_CHANGE);
        double intersectedTwdChangeDegrees = (double) object.get(TwdTransitionJsonSerializer.INTERSECTED_TWD_CHANGE);
        double bearingMinusTwdInDegrees = (double) object.get(TwdTransitionJsonSerializer.BEARING_MINUS_TWD);
        TwdTransition twdTransition = new TwdTransition(new MeterDistance(distanceMeters),
                new MillisecondsDurationImpl(durationMillis), domainFactory.getOrCreateBoatClass(boatClassName),
                new DegreeBearingImpl(twdChangeDegrees), new DegreeBearingImpl(intersectedTwdChangeDegrees),
                new DegreeBearingImpl(bearingMinusTwdInDegrees));
        if (object.containsKey(TwdTransitionJsonSerializer.CORRECT)) {
            boolean correct = (boolean) object.get(TwdTransitionJsonSerializer.CORRECT);
            ManeuverTypeForClassification fromManeuverType = ManeuverTypeForClassification
                    .valueOf((String) object.get(TwdTransitionJsonSerializer.FROM_MANEUVER_TYPE));
            ManeuverTypeForClassification toManeuverType = ManeuverTypeForClassification
                    .valueOf((String) object.get(TwdTransitionJsonSerializer.TO_MANEUVER_TYPE));
            String regattaName = (String) object.get(TwdTransitionJsonSerializer.REGATTA_NAME);
            twdTransition = new LabelledTwdTransition(twdTransition.getDistance(), twdTransition.getDuration(),
                    twdTransition.getBoatClass(), twdTransition.getTwdChange(), twdTransition.getIntersectedTwdChange(),
                    twdTransition.getBearingToPreviousManeuverMinusTwd(), correct, fromManeuverType, toManeuverType,
                    regattaName);
        }
        return twdTransition;
    }

}
