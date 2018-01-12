package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.maneuverdetection.ManeuverWithEstimationData;
import com.sap.sailing.domain.maneuverdetection.ManeuverWithEstimationDataCalculator;
import com.sap.sailing.domain.maneuverdetection.impl.ManeuverWithEstimationDataCalculatorImpl;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.TrackedRace;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuversWithEstimationDataJsonSerializer extends AbstractTrackedRaceDataJsonSerializer {
    public final static String MANEUVERS = "maneuvers";
    public final static String BOAT_CLASS = "boatClass";
    public final static String COMPETITOR_NAME = "competitorName";

    private final BoatClassJsonSerializer boatClassJsonSerializer;
    private final ManeuverWithEstimationDataJsonSerializer maneuverWithEstimationDataJsonSerializer;

    public ManeuversWithEstimationDataJsonSerializer(BoatClassJsonSerializer boatClassJsonSerializer,
            ManeuverWithEstimationDataJsonSerializer maneuverWithEstimationDataJsonSerializer) {
        this.boatClassJsonSerializer = boatClassJsonSerializer;
        this.maneuverWithEstimationDataJsonSerializer = maneuverWithEstimationDataJsonSerializer;
    }

    @Override
    public JSONObject serialize(TrackedRace trackedRace) {
        final JSONObject result = new JSONObject();
        JSONArray byCompetitorJson = new JSONArray();
        result.put(BYCOMPETITOR, byCompetitorJson);
        for (Competitor competitor : trackedRace.getRace().getCompetitors()) {
            final JSONObject forCompetitorJson = new JSONObject();
            byCompetitorJson.add(forCompetitorJson);
            forCompetitorJson.put(COMPETITOR_NAME, competitor.getName());
            forCompetitorJson.put(BOAT_CLASS, boatClassJsonSerializer.serialize(competitor.getBoat().getBoatClass()));
            final JSONArray maneuvers = new JSONArray();
            forCompetitorJson.put(MANEUVERS, maneuvers);
            for (final ManeuverWithEstimationData maneuver : getManeuversWithEstimationData(trackedRace, competitor)) {
                maneuvers.add(maneuverWithEstimationDataJsonSerializer.serialize(maneuver));
            }
        }
        return result;
    }

    private Iterable<ManeuverWithEstimationData> getManeuversWithEstimationData(TrackedRace trackedRace,
            Competitor competitor) {
        Iterable<Maneuver> maneuvers = trackedRace.getManeuvers(competitor, false);
        ManeuverWithEstimationDataCalculator maneuverWithEstimationDataCalculator = new ManeuverWithEstimationDataCalculatorImpl();
        return maneuverWithEstimationDataCalculator.complementManeuversWithEstimationData(trackedRace, competitor,
                maneuvers);
    }

}
