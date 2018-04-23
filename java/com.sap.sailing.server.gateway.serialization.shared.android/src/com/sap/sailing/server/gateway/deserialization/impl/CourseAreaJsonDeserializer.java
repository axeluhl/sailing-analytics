package com.sap.sailing.server.gateway.deserialization.impl;

import java.io.Serializable;
import java.util.UUID;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.CourseAreaJsonSerializer;
import com.sap.sse.util.impl.UUIDHelper;

public class CourseAreaJsonDeserializer implements JsonDeserializer<CourseArea> {

    private SharedDomainFactory factory;

    public CourseAreaJsonDeserializer(SharedDomainFactory factory) {
        this.factory = factory;
    }

    public CourseArea deserialize(JSONObject object)
            throws JsonDeserializationException {
        String name = (String) object.get(CourseAreaJsonSerializer.FIELD_NAME);
        Serializable id = (Serializable) object.get(CourseAreaJsonSerializer.FIELD_ID);

        return factory.getOrCreateCourseArea((UUID) UUIDHelper.tryUuidConversion(id), name);
    }

}
