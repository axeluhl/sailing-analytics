package com.sap.sailing.server.gateway.deserialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.base.impl.DynamicBoat;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.CompetitorAndBoatJsonSerializer;
import com.sap.sse.common.Util.Pair;

public class CompetitorAndBoatJsonDeserializer implements JsonDeserializer<Pair<Competitor, Boat>> {
    private final JsonDeserializer<Competitor> competitorDeserializer;
    private final JsonDeserializer<DynamicBoat> boatDeserializer;
    
    public static CompetitorAndBoatJsonDeserializer create(SharedDomainFactory baseDomainFactory) {
        return new CompetitorAndBoatJsonDeserializer(CompetitorJsonDeserializer.create(baseDomainFactory), BoatJsonDeserializer.create(baseDomainFactory));
    }

    public CompetitorAndBoatJsonDeserializer(JsonDeserializer<Competitor> competitorDeserializer, JsonDeserializer<DynamicBoat> boatDeserializer) {
        this.competitorDeserializer = competitorDeserializer;
        this.boatDeserializer = boatDeserializer;
    }

    @Override
    public Pair<Competitor, Boat> deserialize(JSONObject competitorAndBoatJsonObject) throws JsonDeserializationException {
        Competitor competitor = competitorDeserializer.deserialize(Helpers.getNestedObjectSafe(competitorAndBoatJsonObject, CompetitorAndBoatJsonSerializer.FIELD_COMPETITOR));
        Boat boat = boatDeserializer.deserialize(Helpers.getNestedObjectSafe(competitorAndBoatJsonObject, CompetitorAndBoatJsonSerializer.FIELD_BOAT));
        return new Pair<Competitor, Boat>(competitor, boat);
    }
}
