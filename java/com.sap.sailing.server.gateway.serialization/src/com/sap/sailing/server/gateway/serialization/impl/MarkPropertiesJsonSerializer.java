package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.coursetemplate.MarkProperties;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class MarkPropertiesJsonSerializer implements JsonSerializer<MarkProperties> {

    private static final String FIELD_ID = "id";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_SHORTNAME = "shortName";
    private static final String FIELD_TAGS = "tags";
    private static final String FIELD_HAS_DEVICEUUID = "hasDeviceUuid";
    private static final String FIELD_COLOR = "color";
    private static final String FIELD_SHAPE = "shape";
    private static final String FIELD_PATTERN = "pattern";
    private static final String FIELD_MARKTYPE = "markType";
    private static final String FIELD_FIXED_POSITION_LATDEG = "latDeg";
    private static final String FIELD_FIXED_POSITION_LONDEG = "lonDeg";

    @Override
    public JSONObject serialize(MarkProperties markProperties) {
        JSONObject result = new JSONObject();
        result.put(FIELD_ID, markProperties.getId().toString());
        result.put(FIELD_NAME, markProperties.getName());
        result.put(FIELD_SHORTNAME, markProperties.getShortName());
        
        final JSONArray tags = new JSONArray();
        markProperties.getTags().forEach(tags::add);
        result.put(FIELD_TAGS, tags);
        
        result.put(FIELD_COLOR, markProperties.getColor() != null ? markProperties.getColor().getAsHtml() : null);
        result.put(FIELD_SHAPE, markProperties.getShape());
        result.put(FIELD_PATTERN, markProperties.getPattern());
        result.put(FIELD_MARKTYPE, markProperties.getType());
        result.put(FIELD_HAS_DEVICEUUID, markProperties.getTrackingDeviceIdentifier() != null);
        if (markProperties.getFixedPosition() != null) {
            result.put(FIELD_FIXED_POSITION_LATDEG, markProperties.getFixedPosition().getLatDeg());
            result.put(FIELD_FIXED_POSITION_LONDEG, markProperties.getFixedPosition().getLngDeg());
        } else {
            result.put(FIELD_FIXED_POSITION_LATDEG, null);
            result.put(FIELD_FIXED_POSITION_LONDEG, null);
        }
        return result;
    }

}
