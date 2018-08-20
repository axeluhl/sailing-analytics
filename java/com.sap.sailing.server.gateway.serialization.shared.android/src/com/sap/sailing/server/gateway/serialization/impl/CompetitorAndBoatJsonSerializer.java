package com.sap.sailing.server.gateway.serialization.impl;

import java.util.logging.Logger;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorWithBoat;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sse.common.Util.Pair;

/** 
 * Serializes a competitor AND a boat as a kind of pair (e.g. in the context of a race)
 */
public class CompetitorAndBoatJsonSerializer implements JsonSerializer<Pair<Competitor, Boat>> {
    private static final Logger logger = Logger.getLogger(CompetitorAndBoatJsonSerializer.class.getName());
    
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
        final JSONObject result;
        JSONObject serializedCompetitor = competitorJsonSerializer.serialize(competitorAndBoat.getA());
        // for compatibility, write a CompetitorWithBoat in the same format that a Competitor use to be
        // serialized; only if the competitor has no boat, use the new format. The de-serializer will be
        // able to distinguish based on the presence of the top-level FIELD_COMPETITOR field.
        final boolean compatibilityMode;
        if (competitorAndBoat.getA().hasBoat()) {
            final CompetitorWithBoat competitorWithBoat = (CompetitorWithBoat) competitorAndBoat.getA();
            final Boat boat = competitorWithBoat.getBoat();
            if (boat != competitorAndBoat.getB()) {
                logger.warning("Competitor "+competitorWithBoat+" has boat "+boat+" which is different from the boat to be serialized with it ("+
                        competitorAndBoat.getB());
                compatibilityMode = false;
            } else {
                compatibilityMode = true;
            }
        } else {
            compatibilityMode = false;
        }
        if (compatibilityMode) {
            result = serializedCompetitor;
        } else {
            JSONObject serializedBoat = boatJsonSerializer.serialize(competitorAndBoat.getB());
            result = new JSONObject();
            result.put(FIELD_COMPETITOR, serializedCompetitor);
            result.put(FIELD_BOAT, serializedBoat);
        }
        return result;
    }
}
