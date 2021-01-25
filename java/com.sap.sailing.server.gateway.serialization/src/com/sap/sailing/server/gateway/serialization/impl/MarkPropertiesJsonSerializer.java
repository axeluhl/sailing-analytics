package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.coursetemplate.MarkProperties;
import com.sap.sse.shared.json.JsonSerializer;

public class MarkPropertiesJsonSerializer implements JsonSerializer<MarkProperties> {
    private static final String FIELD_ID = "id";
    private static final String FIELD_POSITIONING = "positioning";

    private final FreestyleMarkPropertiesJsonSerializer commonMarkPropertiesWithTagsJsonSerializer;
    private final PositioningJsonSerializer positioningJsonSerializer;

    public MarkPropertiesJsonSerializer(DeviceIdentifierJsonSerializer deviceIdentifierSerializer) {
        this.commonMarkPropertiesWithTagsJsonSerializer = new FreestyleMarkPropertiesJsonSerializer();
        this.positioningJsonSerializer = new PositioningJsonSerializer(deviceIdentifierSerializer);
    }

    @Override
    public JSONObject serialize(MarkProperties markProperties) {
        JSONObject result = new JSONObject();
        result.put(FIELD_ID, markProperties.getId().toString());
        result.putAll(commonMarkPropertiesWithTagsJsonSerializer.serialize(markProperties));
        result.put(FIELD_POSITIONING, markProperties.getPositioningInformation() == null ? null :
            positioningJsonSerializer.serialize(markProperties.getPositioningInformation()));
        return result;
    }

}
