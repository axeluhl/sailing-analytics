package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.coursetemplate.MarkProperties;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class MarkPropertiesJsonSerializer implements JsonSerializer<MarkProperties> {
    private static final String FIELD_ID = "id";
    private static final String FIELD_TAGS = "tags";
    private static final String FIELD_POSITIONING = "positioning";

    private final CommonMarkPropertiesJsonSerializer commonMarkPropertiesJsonSerializer;
    private final PositioningJsonSerializer positioningJsonSerializer;

    public MarkPropertiesJsonSerializer(DeviceIdentifierJsonSerializer deviceIdentifierSerializer) {
        this.commonMarkPropertiesJsonSerializer = new CommonMarkPropertiesJsonSerializer();
        this.positioningJsonSerializer = new PositioningJsonSerializer(deviceIdentifierSerializer);
    }

    @Override
    public JSONObject serialize(MarkProperties markProperties) {
        JSONObject result = new JSONObject();
        result.put(FIELD_ID, markProperties.getId().toString());
        final JSONArray tags = new JSONArray();
        markProperties.getTags().forEach(tags::add);
        result.put(FIELD_TAGS, tags);
        result.putAll(commonMarkPropertiesJsonSerializer.serialize(markProperties));
        result.put(FIELD_POSITIONING, markProperties.getPositioningInformation() == null ? null :
            positioningJsonSerializer.serialize(markProperties.getPositioningInformation()));
        return result;
    }

}
