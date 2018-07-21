package com.sap.sailing.server.gateway.deserialization.coursedata.impl;

import java.io.Serializable;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.MarkJsonSerializer;
import com.sap.sse.common.Color;
import com.sap.sse.common.impl.AbstractColor;
import com.sap.sse.util.impl.UUIDHelper;

/**
 * Deserializer for marks.
 */
public class MarkDeserializer implements JsonDeserializer<Mark> {

    private SharedDomainFactory factory;

    public MarkDeserializer(SharedDomainFactory factory) {
        this.factory = factory;
    }

    @Override
    public Mark deserialize(JSONObject object) throws JsonDeserializationException {
        Serializable id = UUIDHelper.tryUuidConversion((Serializable) object.get(MarkJsonSerializer.FIELD_ID));
        String colorAsString = (String) object.get(MarkJsonSerializer.FIELD_COLOR);
        Color color = colorAsString == null ? null : AbstractColor.getCssColor(colorAsString);
        String pattern = (String) object.get(MarkJsonSerializer.FIELD_PATTERN);
        String shape = (String) object.get(MarkJsonSerializer.FIELD_SHAPE);
        MarkType type = MarkType.valueOf((String) object.get(MarkJsonSerializer.FIELD_TYPE));
        String name = (String) object.get(MarkJsonSerializer.FIELD_NAME);
        Mark mark = factory.getOrCreateMark(id, name, type, color, shape, pattern);
        return mark;
    }
}
