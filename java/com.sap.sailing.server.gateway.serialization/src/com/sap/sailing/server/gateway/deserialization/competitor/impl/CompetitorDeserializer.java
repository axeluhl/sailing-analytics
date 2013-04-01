package com.sap.sailing.server.gateway.deserialization.competitor.impl;

import java.io.Serializable;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.serialization.competitor.impl.CompetitorJsonSerializer;

public class CompetitorDeserializer extends CompetitorIdDeserializer {

    public CompetitorDeserializer(SharedDomainFactory factory) {
        super(factory);
    }

    @Override
    public Competitor deserialize(JSONObject object) throws JsonDeserializationException {
        Competitor competitor = super.deserialize(object);
        if (competitor == null) {
            Serializable competitorId = (Serializable) object.get(CompetitorJsonSerializer.FIELD_ID);
            String name = (String) object.get(CompetitorJsonSerializer.FIELD_NAME);
            competitor = factory.getOrCreateCompetitor(competitorId, name, null, null);
        }
        return competitor;
    }

}
