package com.sap.sailing.server.gateway.deserialization.impl;

import java.io.Serializable;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.racegroup.impl.CompetitorJsonSerializer;

public class CompetitorDeserializer implements JsonDeserializer<Competitor> {

    protected SharedDomainFactory factory;
    
    public CompetitorDeserializer(SharedDomainFactory factory) {
        this.factory = factory;
    }

    @Override
    public Competitor deserialize(JSONObject object) throws JsonDeserializationException {
        Serializable competitorId = (Serializable) object.get(CompetitorJsonSerializer.FIELD_ID);
        competitorId = Helpers.tryUuidConversion(competitorId);
        String name = (String) object.get(CompetitorJsonSerializer.FIELD_NAME);
        Competitor competitor = factory.getOrCreateCompetitor(competitorId, name, null, null);
        return competitor;
    }

}
