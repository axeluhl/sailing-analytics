package com.sap.sailing.server.gateway.deserialization.coursedata.impl;

import java.io.Serializable;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.MarkJsonSerializer;

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
        Serializable id = Helpers.tryUuidConversion(object.get(MarkJsonSerializer.FIELD_ID).toString());
        String color = object.get(MarkJsonSerializer.FIELD_COLOR).toString();
        String pattern = object.get(MarkJsonSerializer.FIELD_PATTERN).toString();
        String shape = object.get(MarkJsonSerializer.FIELD_SHAPE).toString();
        MarkType type = MarkType.valueOf(object.get(MarkJsonSerializer.FIELD_TYPE).toString());
        String name = object.get(MarkJsonSerializer.FIELD_NAME).toString();
        
        Mark mark = factory.getOrCreateMark(id, name, type, color, shape, pattern);
        return mark;
    }

}
