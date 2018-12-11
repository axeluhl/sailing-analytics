package com.sap.sailing.windestimation.data.deserializer;

import org.json.simple.JSONObject;

import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.windestimation.data.LabelledTwdTransition;
import com.sap.sailing.windestimation.data.TwdTransition;

public class TwdTransitionJsonSerializer implements JsonSerializer<TwdTransition> {

    public static final String DURATION = "duration";
    public static final String DISTANCE = "distance";
    public static final String BOAT_CLASS = "boatClass";
    public static final String TWD_CHANGE = "twdChange";
    public static final String INTERSECTED_TWD_CHANGE = "intersectedTwdChange";
    public static final String CORRECT = "correct";
    public static final String FROM_MANEUVER_TYPE = "fromManeuverType";
    public static final String TO_MANEUVER_TYPE = "toManeuverType";
    public static final String REGATTA_NAME = "regattaName";
    public static final String BEARING_MINUS_TWD = "bearingMinusTwd";

    @Override
    public JSONObject serialize(TwdTransition transition) {
        JSONObject json = new JSONObject();
        json.put(DURATION, transition.getDuration().asMillis());
        json.put(DISTANCE, transition.getDistance().getMeters());
        json.put(BOAT_CLASS, transition.getBoatClass().getName());
        json.put(TWD_CHANGE, transition.getTwdChange().getDegrees());
        json.put(INTERSECTED_TWD_CHANGE, transition.getIntersectedTwdChange().getDegrees());
        if (transition instanceof LabelledTwdTransition) {
            LabelledTwdTransition labelledTransition = (LabelledTwdTransition) transition;
            json.put(CORRECT, labelledTransition.isCorrect());
            json.put(FROM_MANEUVER_TYPE, labelledTransition.getFromManeuverType().name());
            json.put(TO_MANEUVER_TYPE, labelledTransition.getToManeuverType().name());
            json.put(BEARING_MINUS_TWD, labelledTransition.getBearingToPreviousManeuverMinusTwd().getDegrees());
            json.put(REGATTA_NAME, labelledTransition.getRegattaName());
        }
        return json;
    }

}
