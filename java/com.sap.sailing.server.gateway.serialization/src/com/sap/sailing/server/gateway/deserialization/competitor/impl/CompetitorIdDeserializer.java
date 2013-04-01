package com.sap.sailing.server.gateway.deserialization.competitor.impl;

import java.io.Serializable;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.competitor.impl.CompetitorJsonSerializer;

public class CompetitorIdDeserializer implements JsonDeserializer<Competitor> {

    protected SharedDomainFactory factory;

    public CompetitorIdDeserializer(SharedDomainFactory factory) {
        this.factory = factory;
    }

    @Override
    public Competitor deserialize(JSONObject object) throws JsonDeserializationException {
        Serializable competitorId = (Serializable) object.get(CompetitorJsonSerializer.FIELD_ID);
        Competitor competitor = factory.getExistingCompetitorById(competitorId);
        return competitor;
    }

}
