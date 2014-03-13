package com.sap.sailing.server.gateway.deserialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.base.impl.NationalityImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.NationalityJsonSerializer;

public class NationalityJsonDeserializer implements JsonDeserializer<Nationality> {
    private final SharedDomainFactory domainFactory;

    public NationalityJsonDeserializer(SharedDomainFactory domainFactory) {
        this.domainFactory = domainFactory;
    }

    @Override
    public Nationality deserialize(JSONObject object) throws JsonDeserializationException {
        final Nationality result;
        if (object == null) {
            result = null;
        } else {
            String threeLetterIOCAcronym = (String) object.get(NationalityJsonSerializer.FIELD_IOC);
            if (threeLetterIOCAcronym == null) {
                result = null;
            } else {
                result = (Nationality) new NationalityImpl(threeLetterIOCAcronym).resolve(domainFactory);
            }
        }
        return result;
    }
}
