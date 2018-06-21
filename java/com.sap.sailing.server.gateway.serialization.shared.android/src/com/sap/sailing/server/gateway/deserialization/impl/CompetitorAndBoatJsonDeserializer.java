package com.sap.sailing.server.gateway.deserialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.base.impl.DynamicBoat;
import com.sap.sailing.domain.base.impl.DynamicCompetitor;
import com.sap.sailing.domain.base.impl.DynamicCompetitorWithBoat;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.CompetitorAndBoatJsonSerializer;
import com.sap.sse.common.Util.Pair;

public class CompetitorAndBoatJsonDeserializer implements JsonDeserializer<Pair<DynamicCompetitor, Boat>> {
    private final JsonDeserializer<DynamicCompetitor> competitorDeserializer;
    private final JsonDeserializer<DynamicBoat> boatDeserializer;
    
    public static CompetitorAndBoatJsonDeserializer create(SharedDomainFactory baseDomainFactory) {
        return new CompetitorAndBoatJsonDeserializer(CompetitorJsonDeserializer.create(baseDomainFactory), BoatJsonDeserializer.create(baseDomainFactory));
    }

    public CompetitorAndBoatJsonDeserializer(JsonDeserializer<DynamicCompetitor> competitorDeserializer, JsonDeserializer<DynamicBoat> boatDeserializer) {
        this.competitorDeserializer = competitorDeserializer;
        this.boatDeserializer = boatDeserializer;
    }

    @Override
    public Pair<DynamicCompetitor, Boat> deserialize(JSONObject competitorAndBoatJsonObject) throws JsonDeserializationException {
        final DynamicCompetitor competitor;
        final Boat boat;
        if (competitorAndBoatJsonObject.containsKey(CompetitorAndBoatJsonSerializer.FIELD_COMPETITOR)) {
            competitor = competitorDeserializer.deserialize(Helpers.getNestedObjectSafe(competitorAndBoatJsonObject, CompetitorAndBoatJsonSerializer.FIELD_COMPETITOR));
            boat = boatDeserializer.deserialize(Helpers.getNestedObjectSafe(competitorAndBoatJsonObject, CompetitorAndBoatJsonSerializer.FIELD_BOAT));
        } else {
            final DynamicCompetitorWithBoat competitorWithBoat = (DynamicCompetitorWithBoat) competitorDeserializer.deserialize(competitorAndBoatJsonObject);
            competitor = competitorWithBoat;
            boat = competitorWithBoat.getBoat();
        }
        return new Pair<>(competitor, boat);
    }
}
