package com.sap.sailing.server.gateway.deserialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.impl.NationalityImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.NationalityJsonSerializer;

public class NationalityJsonDeserialzer implements JsonDeserializer<Nationality> {

    @Override
    public Nationality deserialize(JSONObject object) throws JsonDeserializationException {
        String threeLetterIOCAcronym = (String) object.get(NationalityJsonSerializer.FIELD_IOC);
        return new NationalityImpl(threeLetterIOCAcronym);
    }
    
    

}
