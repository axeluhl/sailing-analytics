package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sse.common.Util.Pair;

public class RepeatablePartJsonSerializer implements JsonSerializer<Pair<Integer, Integer>> {

    public static final String FIELD_REPEATABLE_PART_START = "zeroBasedIndexOfRepeatablePartStart";
    public static final String FIELD_REPEATABLE_PART_END = "zeroBasedIndexOfRepeatablePartEnd";

    @Override
    public JSONObject serialize(Pair<Integer, Integer> repeatablePart) {
        final JSONObject repeatablePartJSON = new JSONObject();
        repeatablePartJSON.put(FIELD_REPEATABLE_PART_START, repeatablePart.getA());
        repeatablePartJSON.put(FIELD_REPEATABLE_PART_END, repeatablePart.getB());
        return repeatablePartJSON;
    }

}
