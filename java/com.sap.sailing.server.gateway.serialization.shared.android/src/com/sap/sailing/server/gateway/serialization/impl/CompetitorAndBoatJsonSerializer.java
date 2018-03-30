package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sse.common.Util.Pair;

/** 
 * Serializes a competitor AND a boat as a kind of pair (e.g. in the context of a race)
 */
public class CompetitorAndBoatJsonSerializer implements JsonSerializer<Pair<Competitor, Boat>> {
    private final JsonSerializer<Competitor> competitorJsonSerializer;
    private final JsonSerializer<Boat> boatJsonSerializer;

    public static final String FIELD_BOAT = "boat";
    public static final String FIELD_COMPETITOR = "competitor";

    public static CompetitorAndBoatJsonSerializer create() {
        return new CompetitorAndBoatJsonSerializer(CompetitorJsonSerializer.create(), BoatJsonSerializer.create());
    }

    public CompetitorAndBoatJsonSerializer(JsonSerializer<Competitor> competitorJsonSerializer, JsonSerializer<Boat> boatJsonSerializer) {
        this.competitorJsonSerializer = competitorJsonSerializer;
        this.boatJsonSerializer = boatJsonSerializer;
    }

    @Override
    public JSONObject serialize(Pair<Competitor, Boat> competitorAndBoat) {
        JSONObject serializedCompetitorAndBoat = new JSONObject();

        JSONObject serializedCompetitor = competitorJsonSerializer.serialize(competitorAndBoat.getA());
        JSONObject serializedBoat = boatJsonSerializer.serialize(competitorAndBoat.getB());

        serializedCompetitorAndBoat.put(FIELD_COMPETITOR, serializedCompetitor);
        serializedCompetitorAndBoat.put(FIELD_BOAT, serializedBoat);

        return serializedCompetitorAndBoat;
    }
}
