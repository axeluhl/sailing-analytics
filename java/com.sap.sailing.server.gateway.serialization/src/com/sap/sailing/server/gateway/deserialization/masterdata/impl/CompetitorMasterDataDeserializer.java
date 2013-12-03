package com.sap.sailing.server.gateway.deserialization.masterdata.impl;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.UUID;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.base.impl.BoatImpl;
import com.sap.sailing.domain.base.impl.DynamicBoat;
import com.sap.sailing.domain.base.impl.DynamicTeam;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.serialization.masterdata.impl.CompetitorMasterDataJsonSerializer;

public class CompetitorMasterDataDeserializer implements JsonDeserializer<Competitor> {
    
    private final JsonDeserializer<BoatClass> boatClassDeserializer;
    
    private final JsonDeserializer<DynamicTeam> teamDeserializer;
    
    private final SharedDomainFactory domainFactory;
    
    public CompetitorMasterDataDeserializer(JsonDeserializer<BoatClass> boatClassDeserializer,
            JsonDeserializer<DynamicTeam> teamDeserializer, SharedDomainFactory domainFactory) {
        this.boatClassDeserializer = boatClassDeserializer;
        this.teamDeserializer = teamDeserializer;
        this.domainFactory = domainFactory;
    }

    @Override
    public Competitor deserialize(JSONObject object) throws JsonDeserializationException {
        String name = (String) object.get(CompetitorMasterDataJsonSerializer.FIELD_NAME);
        Object idClassName = object.get(CompetitorMasterDataJsonSerializer.FIELD_ID_TYPE);
        Serializable id = (Serializable) object.get(CompetitorMasterDataJsonSerializer.FIELD_ID);
        if (idClassName != null) {
            try {
                Class<?> idClass = Class.forName((String) idClassName);
                if (Number.class.isAssignableFrom(idClass)) {
                    Constructor<?> constructorFromString = idClass.getConstructor(String.class);
                    id = (Serializable) constructorFromString.newInstance(id.toString());
                } else if (UUID.class.isAssignableFrom(idClass)) {
                    id = Helpers.tryUuidConversion(id);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            // To ensure some kind of a backward compability.
            try {
                id = UUID.fromString(id.toString());
            } catch (IllegalArgumentException e) {
                id = id.toString();
            }
        }
        DynamicBoat boat = createBoatFromJson((JSONObject) object.get(CompetitorMasterDataJsonSerializer.FIELD_BOAT));
        DynamicTeam team = teamDeserializer.deserialize((JSONObject) object.get(CompetitorMasterDataJsonSerializer.FIELD_TEAM));
        Competitor competitor;
        if (id instanceof UUID) {
            competitor = domainFactory.getOrCreateDynamicCompetitor((UUID) id, name, team, boat);
        } else {
            competitor = domainFactory.getOrCreateCompetitor(id, name, team, boat);
        }
        return competitor;
        
    }
    
    private DynamicBoat createBoatFromJson(JSONObject object) throws JsonDeserializationException {
        String name = (String) object.get(CompetitorMasterDataJsonSerializer.FIELD_NAME); 
        String sailID = (String) object.get(CompetitorMasterDataJsonSerializer.FIELD_SAIL_ID); 
        BoatClass boatClass = boatClassDeserializer.deserialize((JSONObject) object.get(CompetitorMasterDataJsonSerializer.FIELD_BOAT_CLASS)); 
        return new BoatImpl(name, boatClass, sailID);
    }

}
