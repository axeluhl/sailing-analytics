package com.sap.sailing.server.gateway.serialization.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.domain.maneuverdetection.ManeuverDetectorWithEstimationDataSupport;
import com.sap.sailing.domain.maneuverdetection.TrackTimeInfo;
import com.sap.sailing.domain.maneuverdetection.impl.ManeuverDetectorImpl;
import com.sap.sailing.domain.maneuverdetection.impl.ManeuverDetectorWithEstimationDataSupportDecoratorImpl;
import com.sap.sailing.domain.maneuverdetection.impl.ManeuverSpot;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.tracking.CompleteManeuverCurve;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.TimePoint;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class CompleteManeuverCurvesWithEstimationDataJsonSerializer implements CompetitorTrackElementsJsonSerializer {
    private final PolarDataService polarDataService;
    private final CompleteManeuverCurveWithEstimationDataJsonSerializer maneuverWithEstimationDataJsonSerializer;

    public CompleteManeuverCurvesWithEstimationDataJsonSerializer(PolarDataService polarDataService,
            CompleteManeuverCurveWithEstimationDataJsonSerializer maneuverWithEstimationDataJsonSerializer) {
        this.polarDataService = polarDataService;
        this.maneuverWithEstimationDataJsonSerializer = maneuverWithEstimationDataJsonSerializer;
    }

    @Override
    public JSONArray serialize(TrackedRace trackedRace, Competitor competitor, TimePoint from, TimePoint to,
            TrackTimeInfo trackTimeInfo) {
        final JSONArray completeManeuverCurvesWithEstimationData = new JSONArray();
        Iterable<CompleteManeuverCurveWithEstimationData> completeManeuvers = trackTimeInfo.getTrackStartTimePoint()
                .equals(from) && trackTimeInfo.getTrackEndTimePoint().equals(to)
                        ? getCompleteManeuverCurvesWithEstimationData(trackedRace, competitor)
                        : getCompleteManeuverCurvesWithEstimationData(trackedRace, competitor, from, to);
        for (CompleteManeuverCurveWithEstimationData maneuver : completeManeuvers) {
            completeManeuverCurvesWithEstimationData.add(maneuverWithEstimationDataJsonSerializer.serialize(maneuver));
        }
        return completeManeuverCurvesWithEstimationData;
    }

    private Iterable<CompleteManeuverCurveWithEstimationData> getCompleteManeuverCurvesWithEstimationData(
            TrackedRace trackedRace, Competitor competitor, TimePoint from, TimePoint to) {
        ManeuverDetectorImpl maneuverDetector = new ManeuverDetectorImpl(trackedRace, competitor);
        List<ManeuverSpot> maneuverSpots = maneuverDetector.detectManeuverSpots(from, to);
        List<CompleteManeuverCurve> maneuverCurves = maneuverSpots.stream()
                .map(maneuverSpot -> maneuverSpot.getManeuverCurve()).collect(Collectors.toList());
        ManeuverDetectorWithEstimationDataSupport maneuverDetectorWithEstimationData = new ManeuverDetectorWithEstimationDataSupportDecoratorImpl(
                maneuverDetector, polarDataService);
        Iterable<CompleteManeuverCurveWithEstimationData> maneuversWithEstimationData = maneuverDetectorWithEstimationData
                .getCompleteManeuverCurvesWithEstimationData(maneuverCurves);
        return maneuversWithEstimationData;
    }

    private Iterable<CompleteManeuverCurveWithEstimationData> getCompleteManeuverCurvesWithEstimationData(
            TrackedRace trackedRace, Competitor competitor) {
        Iterable<Maneuver> maneuvers = trackedRace.getManeuvers(competitor, false);
        ManeuverDetectorImpl maneuverDetector = new ManeuverDetectorImpl(trackedRace, competitor);
        ManeuverDetectorWithEstimationDataSupport maneuverDetectorWithEstimationData = new ManeuverDetectorWithEstimationDataSupportDecoratorImpl(
                maneuverDetector, polarDataService);
        Iterable<CompleteManeuverCurve> maneuverCurves = maneuverDetectorWithEstimationData
                .getCompleteManeuverCurves(maneuvers);
        Iterable<CompleteManeuverCurveWithEstimationData> maneuversWithEstimationData = maneuverDetectorWithEstimationData
                .getCompleteManeuverCurvesWithEstimationData(maneuverCurves);
        return maneuversWithEstimationData;
    }
}
