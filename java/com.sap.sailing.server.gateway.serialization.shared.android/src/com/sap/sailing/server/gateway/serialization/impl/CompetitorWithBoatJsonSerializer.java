package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorWithBoat;
import com.sap.sailing.domain.common.tracking.impl.CompetitorJsonConstants;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class CompetitorWithBoatJsonSerializer implements JsonSerializer<CompetitorWithBoat> {
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
    public JSONObject serialize(CompetitorWithBoat competitor) {
        JSONObject serializedCompetitor = competitorJsonSerializer.serialize(competitor);
        serializedCompetitor.put(CompetitorJsonConstants.FIELD_SAIL_ID, competitor.getBoat() == null ? "" : competitor.getBoat().getSailID());
        if (boatJsonSerializer != null && competitor.getBoat() != null) {
            serializedCompetitor.put(CompetitorJsonConstants.FIELD_BOAT, boatJsonSerializer.serialize(competitor.getBoat()));
        }
        return serializedCompetitor;
    }
}
