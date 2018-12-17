package com.sap.sailing.windestimation.data.serialization;

import org.json.simple.JSONObject;

import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.windestimation.data.LabelledTwdTransition;
import com.sap.sailing.windestimation.data.TwdTransition;

public class TwdTransitionJsonSerializer implements JsonSerializer<TwdTransition> {

    public static final String DURATION = "s";
    public static final String DISTANCE = "m";
    public static final String TWD_CHANGE = "deg";
    public static final String INTERSECTED_TWD_CHANGE = "iDeg";
    public static final String CORRECT = "correct";
    public static final String FROM_MANEUVER_TYPE = "from";
    public static final String TO_MANEUVER_TYPE = "to";
    public static final String BEARING_MINUS_TWD = "bMinT"; // bearingMinusTwd
    public static final String TEST_DATASET = "test";

    @Override
    public JSONObject serialize(TwdTransition transition) {
        JSONObject json = new JSONObject();
        json.put(DURATION, transition.getDuration().asSeconds());
        json.put(DISTANCE, transition.getDistance().getMeters());
        json.put(TWD_CHANGE, transition.getTwdChange().getDegrees());
        json.put(INTERSECTED_TWD_CHANGE, transition.getIntersectedTwdChange().getDegrees());
        if (transition instanceof LabelledTwdTransition) {
            LabelledTwdTransition labelledTransition = (LabelledTwdTransition) transition;
            json.put(CORRECT, labelledTransition.isCorrect());
            json.put(FROM_MANEUVER_TYPE, labelledTransition.getFromManeuverType().ordinal());
            json.put(TO_MANEUVER_TYPE, labelledTransition.getToManeuverType().ordinal());
            json.put(BEARING_MINUS_TWD, labelledTransition.getBearingToPreviousManeuverMinusTwd().getDegrees());
            json.put(TEST_DATASET, labelledTransition.isTestDataset());
        }
        return json;
    }

}
