package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.coursetemplate.FreestyleMarkProperties;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class FreestyleMarkPropertiesJsonSerializer implements JsonSerializer<FreestyleMarkProperties> {

    public static final String FIELD_TAGS = "tag";
    
    private final CommonMarkPropertiesJsonSerializer commonMarkPropertiesJsonSerializer;

    public FreestyleMarkPropertiesJsonSerializer() {
        commonMarkPropertiesJsonSerializer = new CommonMarkPropertiesJsonSerializer();
    }

    @Override
    public JSONObject serialize(FreestyleMarkProperties commonMarkProperties) {
        JSONObject result = commonMarkPropertiesJsonSerializer.serialize(commonMarkProperties);
        JSONArray jsonTags = new JSONArray();
        for (String tag : commonMarkProperties.getTags()) {
            jsonTags.add(tag);
        }
        result.put(FIELD_TAGS, jsonTags);
        return result;
    }
}
