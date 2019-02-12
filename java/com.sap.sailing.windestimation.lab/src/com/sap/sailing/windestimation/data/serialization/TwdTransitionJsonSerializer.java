package com.sap.sailing.windestimation.data.serialization;

import org.json.simple.JSONObject;

import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.windestimation.data.LabelledTwdTransition;

public class TwdTransitionJsonSerializer implements JsonSerializer<LabelledTwdTransition> {

    public static final String DURATION = "s";
    public static final String DISTANCE = "m";
    public static final String TWD_CHANGE = "deg";
    public static final String CORRECT = "correct";
    public static final String FROM_MANEUVER_TYPE = "from";
    public static final String TO_MANEUVER_TYPE = "to";
    public static final String TEST_DATASET = "test";

    @Override
    public JSONObject serialize(LabelledTwdTransition transition) {
        JSONObject json = new JSONObject();
        json.put(DURATION, transition.getDuration().asSeconds());
        json.put(DISTANCE, transition.getDistance().getMeters());
        json.put(TWD_CHANGE, transition.getTwdChange().getDegrees());
        json.put(FROM_MANEUVER_TYPE, transition.getFromManeuverType().ordinal());
        json.put(TO_MANEUVER_TYPE, transition.getToManeuverType().ordinal());
        json.put(CORRECT, transition.isCorrect());
        json.put(TEST_DATASET, transition.isTestDataset());
        return json;
    }

}
