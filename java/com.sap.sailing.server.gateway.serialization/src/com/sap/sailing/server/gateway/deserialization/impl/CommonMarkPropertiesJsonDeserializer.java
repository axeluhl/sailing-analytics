package com.sap.sailing.server.gateway.deserialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.domain.coursetemplate.CommonMarkProperties;
import com.sap.sailing.domain.coursetemplate.impl.CommonMarkPropertiesImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.CommonMarkPropertiesJsonSerializer;
import com.sap.sse.common.impl.RGBColor;

public class CommonMarkPropertiesJsonDeserializer
        implements JsonDeserializer<CommonMarkProperties> {

    @Override
    public CommonMarkProperties deserialize(JSONObject json) throws JsonDeserializationException {
        final String name = (String) json.get(CommonMarkPropertiesJsonSerializer.FIELD_NAME);
        final String shortName = (String) json.get(CommonMarkPropertiesJsonSerializer.FIELD_SHORTNAME);
        final String color = (String) json.get(CommonMarkPropertiesJsonSerializer.FIELD_COLOR);
        final String shape = (String) json.get(CommonMarkPropertiesJsonSerializer.FIELD_SHAPE);
        final String pattern = (String) json.get(CommonMarkPropertiesJsonSerializer.FIELD_PATTERN);
        final String type = (String) json.get(CommonMarkPropertiesJsonSerializer.FIELD_MARKTYPE);

        return new CommonMarkPropertiesImpl(name, shortName,
                (color != null && color.length() > 0) ? new RGBColor(color) : null, shape, pattern,
                type != null && type.length() > 0 ? MarkType.valueOf(type) : null);
    }
}
