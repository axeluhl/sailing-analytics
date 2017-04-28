package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorAndBoat;
import com.sap.sailing.domain.common.tracking.impl.CompetitorJsonConstants;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class CompetitorWithBoatJsonSerializer implements JsonSerializer<CompetitorAndBoat> {
    private final JsonSerializer<Boat> boatJsonSerializer;
    private final JsonSerializer<Competitor> competitorJsonSerializer;

    public static CompetitorWithBoatJsonSerializer create() {
        return new CompetitorWithBoatJsonSerializer(CompetitorJsonSerializer.create(), BoatJsonSerializer.create());
    }

    public CompetitorWithBoatJsonSerializer() {
        this(null, null);
    }

    public CompetitorWithBoatJsonSerializer(JsonSerializer<Competitor> competitorJsonSerializer, JsonSerializer<Boat> boatJsonSerializer) {
        this.competitorJsonSerializer = competitorJsonSerializer;
        this.boatJsonSerializer = boatJsonSerializer;
    }

    @Override
    public JSONObject serialize(CompetitorAndBoat competitorAndBoat) {
        JSONObject serializedCompetitor = competitorJsonSerializer.serialize(competitorAndBoat.getCompetitor());
        
        serializedCompetitor.put(CompetitorJsonConstants.FIELD_SAIL_ID, competitorAndBoat.getBoat() == null ? "" : competitorAndBoat.getBoat().getSailID());

        if (boatJsonSerializer != null) {
            serializedCompetitor.put(CompetitorJsonConstants.FIELD_BOAT, boatJsonSerializer.serialize(competitorAndBoat.getBoat()));
        }
        return serializedCompetitor;
    }
}
