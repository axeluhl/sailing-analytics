package com.sap.sailing.server.gateway.deserialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.BoatClassJsonSerializer;

public class BoatClassJsonDeserializer implements JsonDeserializer<BoatClass> {

    private SharedDomainFactory domainFactory;

    public BoatClassJsonDeserializer(SharedDomainFactory factory) {
        this.domainFactory = factory;
    }

    public BoatClass deserialize(JSONObject object) throws JsonDeserializationException {
        String name = (String) object.get(BoatClassJsonSerializer.FIELD_NAME);
        
        if (object.containsKey(BoatClassJsonSerializer.FIELD_TYPICALLY_STARTS_UPWIND)) {
        	boolean typicallyStartsUpwind = Boolean.parseBoolean(object.get(BoatClassJsonSerializer.FIELD_TYPICALLY_STARTS_UPWIND).toString());
            return domainFactory.getOrCreateBoatClass(name, typicallyStartsUpwind);
        }
        
        return domainFactory.getOrCreateBoatClass(name);
    }

}
