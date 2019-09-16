package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.coursetemplate.MarkProperties;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class MarkPropertiesJsonSerializer implements JsonSerializer<MarkProperties> {

    private static final String FIELD_ID = "id";
    private static final String FIELD_TAGS = "tags";
    private static final String FIELD_HAS_DEVICEUUID = "hasDeviceUuid";
    private static final String FIELD_FIXED_POSITION_LATDEG = "latDeg";
    private static final String FIELD_FIXED_POSITION_LONDEG = "lonDeg";

    private CommonMarkPropertiesJsonSerializer commonMarkPropertiesJsonSerializer;

    public MarkPropertiesJsonSerializer() {
        this.commonMarkPropertiesJsonSerializer = new CommonMarkPropertiesJsonSerializer();
    }

    @Override
    public JSONObject serialize(MarkProperties markProperties) {
        JSONObject result = new JSONObject();
        result.put(FIELD_ID, markProperties.getId().toString());
        final JSONArray tags = new JSONArray();
        markProperties.getTags().forEach(tags::add);
        result.put(FIELD_TAGS, tags);
        result.putAll(commonMarkPropertiesJsonSerializer.serialize(markProperties));
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
