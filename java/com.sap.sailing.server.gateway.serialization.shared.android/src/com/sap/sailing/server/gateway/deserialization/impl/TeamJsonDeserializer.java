package com.sap.sailing.server.gateway.deserialization.impl;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.base.impl.DynamicPerson;
import com.sap.sailing.domain.base.impl.DynamicTeam;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.TeamJsonSerializer;

public class TeamJsonDeserializer implements JsonDeserializer<DynamicTeam> {

    private final JsonDeserializer<DynamicPerson> personDeserializer;
    private static final Logger logger = Logger.getLogger(TeamJsonDeserializer.class.getName());

    public static TeamJsonDeserializer create(SharedDomainFactory baseDomainFactory) {
        return new TeamJsonDeserializer(new PersonJsonDeserializer(new NationalityJsonDeserializer(baseDomainFactory)));
    }

    public TeamJsonDeserializer(JsonDeserializer<DynamicPerson> personDeserializer) {
        this.personDeserializer = personDeserializer;
    }

    @Override
    public DynamicTeam deserialize(JSONObject object) throws JsonDeserializationException {
        String name = (String) object.get(TeamJsonSerializer.FIELD_NAME);
        DynamicPerson coach = personDeserializer.deserialize((JSONObject) object.get(TeamJsonSerializer.FIELD_COACH));
        Set<DynamicPerson> sailors = new HashSet<DynamicPerson>();
        
        JSONArray sailorsJson = (JSONArray) object.get(TeamJsonSerializer.FIELD_SAILORS);
        for (Object sailorObject : sailorsJson) {
            sailors.add(personDeserializer.deserialize((JSONObject) sailorObject));
        }
        
        String imageURIAsString = (String) object.get(TeamJsonSerializer.FIELD_IMAGE_URI);
        if (imageURIAsString != null){
        	try {
        		URI imageURI = URI.create(imageURIAsString);
        		return new TeamImpl(name, sailors, coach, imageURI);
        	} catch (IllegalArgumentException e){
        		logger.warning("Illegal team image URI "+e.getMessage());
        	}
        }
        
        return new TeamImpl(name, sailors, coach);
    }

}
