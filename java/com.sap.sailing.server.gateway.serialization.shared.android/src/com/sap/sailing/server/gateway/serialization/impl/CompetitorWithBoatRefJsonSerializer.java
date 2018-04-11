package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorWithBoat;
import com.sap.sailing.domain.common.tracking.impl.CompetitorJsonConstants;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class CompetitorWithBoatRefJsonSerializer implements JsonSerializer<CompetitorWithBoat> {
    private final JsonSerializer<Competitor> competitorJsonSerializer;

    public static CompetitorWithBoatRefJsonSerializer create() {
        return new CompetitorWithBoatRefJsonSerializer(CompetitorJsonSerializer.create(/* serialize boat */ false));
    }

    public CompetitorWithBoatRefJsonSerializer(JsonSerializer<Competitor> competitorJsonSerializer) {
        this.competitorJsonSerializer = competitorJsonSerializer;
    }

    @Override
    public JSONObject serialize(CompetitorWithBoat competitor) {
        JSONObject serializedCompetitor = competitorJsonSerializer.serialize(competitor);
        Boat boat = competitor.getBoat();
        if (boat != null) {
            serializedCompetitor.put(CompetitorJsonConstants.FIELD_BOAT_ID_TYPE, boat.getId().getClass().getName());
            serializedCompetitor.put(CompetitorJsonConstants.FIELD_BOAT_ID, boat.getId().toString());
        }
        return serializedCompetitor;
    }
}
