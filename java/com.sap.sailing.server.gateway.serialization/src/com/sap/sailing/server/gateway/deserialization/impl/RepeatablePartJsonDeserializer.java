package com.sap.sailing.server.gateway.deserialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.RepeatablePartJsonSerializer;
import com.sap.sse.common.Util.Pair;

public class RepeatablePartJsonDeserializer implements JsonDeserializer<Pair<Integer, Integer>> {

    @Override
    public Pair<Integer, Integer> deserialize(JSONObject json) throws JsonDeserializationException {
        final Integer zeroBasedIndexOfRepeatablePartStart = Integer
                .valueOf((String) json.get(RepeatablePartJsonSerializer.FIELD_REPEATABLE_PART_START));
        final Integer zeroBasedIndexOfRepeatablePartEnd = Integer
                .valueOf((String) json.get(RepeatablePartJsonSerializer.FIELD_REPEATABLE_PART_END));
        return new Pair<>(zeroBasedIndexOfRepeatablePartStart, zeroBasedIndexOfRepeatablePartEnd);
    }
}
