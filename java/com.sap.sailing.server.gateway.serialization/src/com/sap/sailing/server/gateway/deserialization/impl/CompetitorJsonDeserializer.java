package com.sap.sailing.server.gateway.deserialization.impl;

import java.io.Serializable;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.base.Team;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.CompetitorJsonSerializer;

public class CompetitorJsonDeserializer implements JsonDeserializer<Competitor> {
    protected final SharedDomainFactory factory;
    protected final JsonDeserializer<Team> teamJsonDeserializer;
    protected final JsonDeserializer<Boat> boatJsonDeserializer;

    public static CompetitorJsonDeserializer create(DomainFactory baseDomainFactory) {
        return new CompetitorJsonDeserializer(baseDomainFactory, new TeamJsonDeserializer(new PersonJsonDeserializer(
                new NationalityJsonDeserializer(baseDomainFactory))), new BoatJsonDeserializer(new BoatClassJsonDeserializer(baseDomainFactory)));
    }

    public CompetitorJsonDeserializer(SharedDomainFactory factory) {
        this(factory, null, /* boatDeserializer */ null);
    }

    public CompetitorJsonDeserializer(SharedDomainFactory factory, JsonDeserializer<Team> teamJsonDeserializer, JsonDeserializer<Boat> boatDeserializer) {
        this.factory = factory;
        this.teamJsonDeserializer = teamJsonDeserializer;
        this.boatJsonDeserializer = boatDeserializer;
    }

    @Override
    public Competitor deserialize(JSONObject object) throws JsonDeserializationException {
        Serializable competitorId = (Serializable) object.get(CompetitorJsonSerializer.FIELD_ID);
        competitorId = Helpers.tryUuidConversion(competitorId);
        String name = (String) object.get(CompetitorJsonSerializer.FIELD_NAME);
        Team team = null;
        Boat boat = null;
        if (teamJsonDeserializer != null) {
            team = teamJsonDeserializer.deserialize(Helpers.getNestedObjectSafe(object,
                    CompetitorJsonSerializer.FIELD_TEAM));
        }
        if (boatJsonDeserializer != null) {
            boat = boatJsonDeserializer.deserialize(Helpers.getNestedObjectSafe(object,
                    CompetitorJsonSerializer.FIELD_BOAT));
        }
        Competitor competitor = factory.getOrCreateCompetitor(competitorId, name, team, boat);
        return competitor;
    }
}
