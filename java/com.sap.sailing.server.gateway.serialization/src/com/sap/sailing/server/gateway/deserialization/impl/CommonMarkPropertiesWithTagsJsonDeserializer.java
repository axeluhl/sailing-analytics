package com.sap.sailing.server.gateway.deserialization.impl;

import java.util.HashSet;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.coursetemplate.CommonMarkProperties;
import com.sap.sailing.domain.coursetemplate.FreestyleMarkProperties;
import com.sap.sailing.domain.coursetemplate.impl.FreestyleMarkPropertiesImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.CommonMarkPropertiesWithTagsJsonSerializer;

public class CommonMarkPropertiesWithTagsJsonDeserializer
        implements JsonDeserializer<FreestyleMarkProperties> {
    private final CommonMarkPropertiesJsonDeserializer commonMarkPropertiesJsonDeserializer;
    
    public CommonMarkPropertiesWithTagsJsonDeserializer() {
        this.commonMarkPropertiesJsonDeserializer = new CommonMarkPropertiesJsonDeserializer();
    }

    @Override
    public FreestyleMarkProperties deserialize(JSONObject json) throws JsonDeserializationException {
        JSONArray tagsArray = Helpers.getNestedArraySafe(json, CommonMarkPropertiesWithTagsJsonSerializer.FIELD_TAGS);
        final Set<String> tags = new HashSet<>();
        for(Object tag:tagsArray) {
            tags.add(tag.toString());
        }
        CommonMarkProperties commonMarkProperties = commonMarkPropertiesJsonDeserializer.deserialize(json);
        return new FreestyleMarkPropertiesImpl(commonMarkProperties, tags);
    }
}
