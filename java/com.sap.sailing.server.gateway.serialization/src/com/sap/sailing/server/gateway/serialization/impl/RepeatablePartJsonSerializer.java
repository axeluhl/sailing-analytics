package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.coursetemplate.RepeatablePart;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RepeatablePartJsonSerializer implements JsonSerializer<RepeatablePart> {

    public static final String FIELD_REPEATABLE_PART_START = "zeroBasedIndexOfRepeatablePartStart";
    public static final String FIELD_REPEATABLE_PART_END = "zeroBasedIndexOfRepeatablePartEnd";

    @Override
    public JSONObject serialize(RepeatablePart repeatablePart) {
        final JSONObject repeatablePartJSON = new JSONObject();
        repeatablePartJSON.put(FIELD_REPEATABLE_PART_START, repeatablePart.getZeroBasedIndexOfRepeatablePartStart());
        repeatablePartJSON.put(FIELD_REPEATABLE_PART_END, repeatablePart.getZeroBasedIndexOfRepeatablePartEnd());
        return repeatablePartJSON;
    }

}
