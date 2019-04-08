package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sse.common.Util.Pair;

public class ManeuversJsonSerializer implements JsonSerializer<Iterable<Pair<Competitor, Iterable<Maneuver>>>> {
    public final static String MANEUVERS = "maneuvers";
    private final ManeuverJsonSerializer maneuverSerializer;

    public ManeuversJsonSerializer(ManeuverJsonSerializer maneuverSerializer) {
        this.maneuverSerializer = maneuverSerializer;
    }

    @Override
    public JSONObject serialize(Iterable<Pair<Competitor, Iterable<Maneuver>>> data) {
        final JSONObject result = new JSONObject();
        JSONArray byCompetitorJson = new JSONArray();
        result.put(AbstractTrackedRaceDataJsonSerializer.BYCOMPETITOR, byCompetitorJson);
        for (Pair<Competitor, Iterable<Maneuver>> singleCompetitorData : data) {
            final JSONObject forCompetitorJson = new JSONObject();
            byCompetitorJson.add(forCompetitorJson);
            final Competitor competitor = singleCompetitorData.getA();
            forCompetitorJson.put(AbstractTrackedRaceDataJsonSerializer.COMPETITOR, String.valueOf(competitor.getId()));
            final JSONArray maneuvers = new JSONArray();
            forCompetitorJson.put(MANEUVERS, maneuvers);
            for (final Maneuver maneuver : singleCompetitorData.getB()) {
                maneuvers.add(maneuverSerializer.serialize(maneuver));
            }
        }
        return result;
    }
}
