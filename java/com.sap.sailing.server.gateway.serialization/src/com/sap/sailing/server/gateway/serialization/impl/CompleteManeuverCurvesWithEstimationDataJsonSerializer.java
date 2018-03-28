package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.domain.maneuverdetection.ManeuverDetector;
import com.sap.sailing.domain.maneuverdetection.impl.ManeuverDetectorImpl;
import com.sap.sailing.domain.tracking.CompleteManeuverCurve;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.TrackedRace;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class CompleteManeuverCurvesWithEstimationDataJsonSerializer extends AbstractTrackedRaceDataJsonSerializer {
    public final static String MANEUVER_CURVES = "maneuverCurves";
    public final static String BOAT_CLASS = "boatClass";
    public final static String COMPETITOR_NAME = "competitorName";

    private final BoatClassJsonSerializer boatClassJsonSerializer;
    private final CompleteManeuverCurveWithEstimationDataJsonSerializer maneuverWithEstimationDataJsonSerializer;

    public CompleteManeuverCurvesWithEstimationDataJsonSerializer(BoatClassJsonSerializer boatClassJsonSerializer,
            CompleteManeuverCurveWithEstimationDataJsonSerializer maneuverWithEstimationDataJsonSerializer) {
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
            final JSONArray completeManeuverCurvesWithEstimationData = new JSONArray();
            for (CompleteManeuverCurveWithEstimationData maneuver : getCompleteManeuverCurvesWithEstimationData(trackedRace,
                    competitor)) {
                completeManeuverCurvesWithEstimationData.add(maneuverWithEstimationDataJsonSerializer.serialize(maneuver));
            }
            forCompetitorJson.put(MANEUVER_CURVES, completeManeuverCurvesWithEstimationData);
        }
        return result;
    }

    private Iterable<CompleteManeuverCurveWithEstimationData> getCompleteManeuverCurvesWithEstimationData(TrackedRace trackedRace,
            Competitor competitor) {
        Iterable<Maneuver> maneuvers = trackedRace.getManeuvers(competitor, false);
        ManeuverDetector maneuverDetector = new ManeuverDetectorImpl(trackedRace, competitor);
        Iterable<CompleteManeuverCurveWithEstimationData> maneuversWithEstimationData = null;
        try {
            Iterable<CompleteManeuverCurve> maneuverCurves = maneuverDetector.getCompleteManeuverCurves(maneuvers);
            maneuversWithEstimationData = maneuverDetector.getCompleteManeuverCurvesWithEstimationData(maneuverCurves);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return maneuversWithEstimationData;
    }

}
