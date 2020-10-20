package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.coursetemplate.CommonMarkPropertiesWithTags;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class CommonMarkPropertiesWithTagsJsonSerializer implements JsonSerializer<CommonMarkPropertiesWithTags> {

    public static final String FIELD_TAGS = "tags";
    
    private final CommonMarkPropertiesJsonSerializer commonMarkPropertiesJsonSerializer;

    public CommonMarkPropertiesWithTagsJsonSerializer() {
        commonMarkPropertiesJsonSerializer = new CommonMarkPropertiesJsonSerializer();
    }

    @Override
    public JSONObject serialize(CommonMarkPropertiesWithTags commonMarkProperties) {
        JSONObject result = commonMarkPropertiesJsonSerializer.serialize(commonMarkProperties);
        result.put(FIELD_TAGS, commonMarkProperties.getTags().toString());
        return result;
    }
}
