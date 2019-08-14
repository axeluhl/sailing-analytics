package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.coursetemplate.MarkProperties;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class MarkPropertiesJsonSerializer implements JsonSerializer<MarkProperties> {

    private static final String FIELD_NAME = "name";
    private static final String FIELD_SHORTNAME = "shortName";
    private static final String FIELD_HAS_DEVICEUUID = "hasDeviceUuid";
    private static final String FIELD_COLOR = "color";
    private static final String FIELD_SHAPE = "shape";
    private static final String FIELD_PATTERN = "pattern";
    private static final String FIELD_MARKTYPE = "markType";

    @Override
    public JSONObject serialize(MarkProperties markProperties) {
        JSONObject result = new JSONObject();
        result.put(FIELD_NAME, markProperties.getName());
        result.put(FIELD_SHORTNAME, markProperties.getShortName());
        result.put(FIELD_COLOR, markProperties.getColor() != null ? markProperties.getColor().getAsHtml() : null);
        result.put(FIELD_SHAPE, markProperties.getShape());
        result.put(FIELD_SHAPE, markProperties.getPattern());
        result.put(FIELD_PATTERN, markProperties.getPattern());
        result.put(FIELD_MARKTYPE, markProperties.getType());
        result.put(FIELD_HAS_DEVICEUUID, markProperties.getTrackingDeviceIdentifier() != null);
        return result;
    }

}
