package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class ManeuversJsonSerializer extends AbstractTrackedRaceDataJsonSerializer {
    public final static String MANEUVERS = "maneuvers";
    
    private final CompetitorJsonSerializer competitorSerializer;
    private final ManeuverJsonSerializer maneuverSerializer;

    public ManeuversJsonSerializer(CompetitorJsonSerializer competitorSerializer, ManeuverJsonSerializer maneuverSerializer) {
        super();
        this.competitorSerializer = competitorSerializer;
        this.maneuverSerializer = maneuverSerializer;
    }

    @Override
    public JSONObject serialize(TrackedRace trackedRace) {
        final JSONObject result = new JSONObject();
        JSONArray byCompetitorJson = new JSONArray();
        result.put(BYCOMPETITOR, byCompetitorJson);
        for (Competitor competitor : trackedRace.getRace().getCompetitors()) {
            final JSONObject forCompetitorJson = new JSONObject();
            byCompetitorJson.add(forCompetitorJson);
            forCompetitorJson.put(COMPETITOR, competitorSerializer.serialize(competitor));
            final JSONArray maneuvers = new JSONArray();
            forCompetitorJson.put(MANEUVERS, maneuvers);
            for (final Maneuver maneuver : getManeuversDuringRace(trackedRace, competitor)) {
                maneuvers.add(maneuverSerializer.serialize(maneuver));
            }
        }
        return result;
    }

    private Iterable<Maneuver> getManeuversDuringRace(TrackedRace trackedRace, Competitor competitor) {
        final TimePoint startTime = trackedRace.getStartOfRace();
        final TimePoint endOfRace = trackedRace.getEndOfRace();
        final TimePoint endTime;
        if (endOfRace != null) {
            endTime = endOfRace;
        } else {
            final TimePoint endOfTracking = trackedRace.getEndOfTracking();
            if (endOfTracking == null || endOfTracking.after(MillisecondsTimePoint.now())) {
                endTime = MillisecondsTimePoint.now();
            } else {
                endTime = endOfTracking;
            }
        }
        return trackedRace.getManeuvers(competitor, startTime, endTime, /* waitForLatest */ false);
    }

}
