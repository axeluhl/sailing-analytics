package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.coursetemplate.MarkTemplate;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class MarkTemplateJsonSerializer implements JsonSerializer<MarkTemplate> {

    private static final String FIELD_ID = "id";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_SHORTNAME = "shortName";
    private static final String FIELD_COLOR = "color";
    private static final String FIELD_SHAPE = "shape";
    private static final String FIELD_PATTERN = "pattern";
    private static final String FIELD_MARKTYPE = "markType";

    @Override
    public JSONObject serialize(MarkTemplate markTemplate) {
        JSONObject result = new JSONObject();
        result.put(FIELD_ID, markTemplate.getId().toString());
        result.put(FIELD_NAME, markTemplate.getName());
        result.put(FIELD_SHORTNAME, markTemplate.getShortName());
        result.put(FIELD_COLOR, markTemplate.getColor() != null ? markTemplate.getColor().getAsHtml() : null);
        result.put(FIELD_SHAPE, markTemplate.getShape());
        result.put(FIELD_PATTERN, markTemplate.getPattern());
        result.put(FIELD_MARKTYPE, markTemplate.getType());
        return result;
    }

}
