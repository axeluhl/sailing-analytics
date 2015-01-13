package com.sap.sailing.domain.common.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.PolarSheetsXYDiagramData;
import com.sap.sailing.domain.common.Tack;
import com.sap.sse.common.Util.Pair;

public class PolarSheetsXYDiagramDataImpl implements PolarSheetsXYDiagramData {

    private static final long serialVersionUID = 778667444303004468L;

    PolarSheetsXYDiagramDataImpl() {
    }

    private Map<Pair<LegType, Tack>, List<Pair<Double, Double>>> pointsForAverageSpeedMovingAverage;

    private Map<Pair<LegType, Tack>, List<Pair<Double, Double>>> pointsForAverageConfidence;

    public PolarSheetsXYDiagramDataImpl(List<Pair<Double, Double>> pointsForUpwindStarboardAverageSpeedMovingAverage,
            List<Pair<Double, Double>> pointsForUpwindStarboardAverageConfidence,
            List<Pair<Double, Double>> pointsForUpwindPortAverageSpeedMovingAverage,
            List<Pair<Double, Double>> pointsForUpwindPortAverageConfidence,
            List<Pair<Double, Double>> pointsForDownwindStarboardAverageSpeedMovingAverage,
            List<Pair<Double, Double>> pointsForDownwindStarboardAverageConfidence,
            List<Pair<Double, Double>> pointsForDownwindPortAverageSpeedMovingAverage,
            List<Pair<Double, Double>> pointsForDownwindPortAverageConfidence) {
        pointsForAverageSpeedMovingAverage = new HashMap<Pair<LegType, Tack>, List<Pair<Double, Double>>>();
        pointsForAverageConfidence = new HashMap<Pair<LegType, Tack>, List<Pair<Double, Double>>>();
        pointsForAverageSpeedMovingAverage.put(new Pair<LegType, Tack>(LegType.UPWIND, Tack.STARBOARD),
                pointsForUpwindStarboardAverageSpeedMovingAverage);
        pointsForAverageConfidence.put(new Pair<LegType, Tack>(LegType.UPWIND, Tack.STARBOARD),
                pointsForUpwindStarboardAverageConfidence);
        pointsForAverageSpeedMovingAverage.put(new Pair<LegType, Tack>(LegType.UPWIND, Tack.PORT),
                pointsForUpwindPortAverageSpeedMovingAverage);
        pointsForAverageConfidence.put(new Pair<LegType, Tack>(LegType.UPWIND, Tack.PORT),
                pointsForUpwindPortAverageConfidence);
        pointsForAverageSpeedMovingAverage.put(new Pair<LegType, Tack>(LegType.DOWNWIND, Tack.STARBOARD),
                pointsForDownwindStarboardAverageSpeedMovingAverage);
        pointsForAverageConfidence.put(new Pair<LegType, Tack>(LegType.DOWNWIND, Tack.STARBOARD),
                pointsForDownwindStarboardAverageConfidence);
        pointsForAverageSpeedMovingAverage.put(new Pair<LegType, Tack>(LegType.DOWNWIND, Tack.PORT),
                pointsForDownwindPortAverageSpeedMovingAverage);
        pointsForAverageConfidence.put(new Pair<LegType, Tack>(LegType.DOWNWIND, Tack.PORT),
                pointsForDownwindPortAverageConfidence);
    }

    @Override
    public List<Pair<Double, Double>> getPointsForAverageSpeedMovingAverage(Tack tack, LegType legType) {
        return pointsForAverageSpeedMovingAverage.get(new Pair<LegType, Tack>(legType, tack));
    }

    @Override
    public List<Pair<Double, Double>> getPointsForAverageConfidence(Tack tack, LegType legType) {
        return pointsForAverageConfidence.get(new Pair<LegType, Tack>(legType, tack));
    }

}
