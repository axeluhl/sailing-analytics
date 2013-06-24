package com.sap.sailing.server.gateway.deserialization.masterdata.impl;

import java.util.UUID;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.base.Team;
import com.sap.sailing.domain.base.impl.BoatImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.masterdata.impl.CompetitorMasterDataJsonSerializer;

public class CompetitorMasterDataDeserializer implements JsonDeserializer<Competitor> {
    
    private final JsonDeserializer<BoatClass> boatClassDeserializer;
    
    private final JsonDeserializer<Team> teamDeserializer;
    
    private final SharedDomainFactory domainFactory;
    
    

    public CompetitorMasterDataDeserializer(JsonDeserializer<BoatClass> boatClassDeserializer,
            JsonDeserializer<Team> teamDeserializer, SharedDomainFactory domainFactory) {
        this.boatClassDeserializer = boatClassDeserializer;
        this.teamDeserializer = teamDeserializer;
        this.domainFactory = domainFactory;
    }

    @Override
    public Competitor deserialize(JSONObject object) throws JsonDeserializationException {
        String name = (String) object.get(CompetitorMasterDataJsonSerializer.FIELD_NAME);
        String id = (String) object.get(CompetitorMasterDataJsonSerializer.FIELD_ID);
        Boat boat = createBoatFromJson((JSONObject) object.get(CompetitorMasterDataJsonSerializer.FIELD_BOAT));
        Team team = teamDeserializer.deserialize((JSONObject) object.get(CompetitorMasterDataJsonSerializer.FIELD_TEAM));
        return domainFactory.getOrCreateCompetitor(UUID.fromString(id), name, team, boat);
        
    }
    
    private Boat createBoatFromJson(JSONObject object) throws JsonDeserializationException {
        String name = (String) object.get(CompetitorMasterDataJsonSerializer.FIELD_NAME); 
        String sailID = (String) object.get(CompetitorMasterDataJsonSerializer.FIELD_SAIL_ID); 
        BoatClass boatClass = boatClassDeserializer.deserialize((JSONObject) object.get(CompetitorMasterDataJsonSerializer.FIELD_BOAT_CLASS)); 
        return new BoatImpl(name, boatClass, sailID);
    }

}
