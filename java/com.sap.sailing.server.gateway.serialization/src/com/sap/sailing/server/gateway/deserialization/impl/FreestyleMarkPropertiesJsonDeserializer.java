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
import com.sap.sailing.server.gateway.serialization.impl.FreestyleMarkPropertiesJsonSerializer;

public class FreestyleMarkPropertiesJsonDeserializer implements JsonDeserializer<FreestyleMarkProperties> {
    private final CommonMarkPropertiesJsonDeserializer commonMarkPropertiesJsonDeserializer;

    public FreestyleMarkPropertiesJsonDeserializer() {
        this.commonMarkPropertiesJsonDeserializer = new CommonMarkPropertiesJsonDeserializer();
    }

    @Override
    public FreestyleMarkProperties deserialize(JSONObject json) throws JsonDeserializationException {
        Object tagsArray = json.get(FreestyleMarkPropertiesJsonSerializer.FIELD_TAGS);
        final Set<String> tags = new HashSet<>();
        if (tagsArray != null && tagsArray instanceof JSONArray) {
            for (Object tag : (JSONArray) tagsArray) {
                tags.add(tag.toString());
            }
        }
        CommonMarkProperties commonMarkProperties = commonMarkPropertiesJsonDeserializer.deserialize(json);
        return new FreestyleMarkPropertiesImpl(commonMarkProperties, tags);
    }
}
