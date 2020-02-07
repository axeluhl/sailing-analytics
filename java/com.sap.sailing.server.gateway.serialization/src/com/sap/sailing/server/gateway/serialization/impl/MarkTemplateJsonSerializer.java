package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.coursetemplate.MarkTemplate;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class MarkTemplateJsonSerializer implements JsonSerializer<MarkTemplate> {

    public static final String FIELD_ID = "id";

    private CommonMarkPropertiesJsonSerializer commonMarkPropertiesJsonSerializer;

    public MarkTemplateJsonSerializer() {
        this.commonMarkPropertiesJsonSerializer = new CommonMarkPropertiesJsonSerializer();
    }

    @Override
    public JSONObject serialize(MarkTemplate markTemplate) {
        JSONObject result = new JSONObject();
        result.put(FIELD_ID, markTemplate.getId().toString());
        result.putAll(commonMarkPropertiesJsonSerializer.serialize(markTemplate));
        return result;
    }

}
