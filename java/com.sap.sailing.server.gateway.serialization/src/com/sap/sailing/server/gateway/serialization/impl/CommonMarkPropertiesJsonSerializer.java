package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.coursetemplate.CommonMarkProperties;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class CommonMarkPropertiesJsonSerializer implements JsonSerializer<CommonMarkProperties> {

    private static final String FIELD_NAME = "name";
    private static final String FIELD_SHORTNAME = "shortName";
    private static final String FIELD_COLOR = "color";
    private static final String FIELD_SHAPE = "shape";
    private static final String FIELD_PATTERN = "pattern";
    private static final String FIELD_MARKTYPE = "markType";

    @Override
    public JSONObject serialize(CommonMarkProperties commonMarkProperties) {
        JSONObject result = new JSONObject();
        result.put(FIELD_NAME, commonMarkProperties.getName());
        result.put(FIELD_SHORTNAME, commonMarkProperties.getShortName());
        result.put(FIELD_COLOR,
                commonMarkProperties.getColor() != null ? commonMarkProperties.getColor().getAsHtml() : null);
        result.put(FIELD_SHAPE, commonMarkProperties.getShape());
        result.put(FIELD_PATTERN, commonMarkProperties.getPattern());
        result.put(FIELD_MARKTYPE, commonMarkProperties.getType());
        return result;
    }
}
