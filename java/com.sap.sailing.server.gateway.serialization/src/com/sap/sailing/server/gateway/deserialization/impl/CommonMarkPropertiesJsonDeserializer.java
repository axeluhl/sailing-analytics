package com.sap.sailing.server.gateway.deserialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.domain.coursetemplate.CommonMarkProperties;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.CommonMarkPropertiesJsonSerializer;
import com.sap.sse.common.Color;
import com.sap.sse.common.impl.RGBColor;

public class CommonMarkPropertiesJsonDeserializer
        implements JsonDeserializer<CommonMarkPropertiesJsonDeserializer.DeserializerCommonMarkPropertiesImpl> {

    @Override
    public DeserializerCommonMarkPropertiesImpl deserialize(JSONObject json) throws JsonDeserializationException {
        final String name = (String) json.get(CommonMarkPropertiesJsonSerializer.FIELD_NAME);
        final String shortName = (String) json.get(CommonMarkPropertiesJsonSerializer.FIELD_SHORTNAME);
        final String color = (String) json.get(CommonMarkPropertiesJsonSerializer.FIELD_COLOR);
        final String shape = (String) json.get(CommonMarkPropertiesJsonSerializer.FIELD_SHAPE);
        final String pattern = (String) json.get(CommonMarkPropertiesJsonSerializer.FIELD_PATTERN);
        final String type = (String) json.get(CommonMarkPropertiesJsonSerializer.FIELD_MARKTYPE);

        return new DeserializerCommonMarkPropertiesImpl(name, shortName,
                (color != null && color.length() > 0) ? new RGBColor(color) : null, shape, pattern,
                type != null && type.length() > 0 ? MarkType.valueOf(type) : null);
    }

    public static class DeserializerCommonMarkPropertiesImpl implements CommonMarkProperties {
        private static final long serialVersionUID = 596082269133825942L;
        private String name;
        private String shortName;
        private Color color;
        private String shape;
        private String pattern;
        private MarkType markType;

        public DeserializerCommonMarkPropertiesImpl(String name, String shortName, Color color, String shape,
                String pattern, MarkType markType) {
            this.name = name;
            this.shortName = shortName;
            this.color = color;
            this.shape = shape;
            this.pattern = pattern;
            this.markType = markType;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Color getColor() {
            return color;
        }

        @Override
        public String getShape() {
            return shape;
        }

        @Override
        public String getPattern() {
            return pattern;
        }

        @Override
        public MarkType getType() {
            return markType;
        }

        @Override
        public String getShortName() {
            return shortName;
        }

    }
}
