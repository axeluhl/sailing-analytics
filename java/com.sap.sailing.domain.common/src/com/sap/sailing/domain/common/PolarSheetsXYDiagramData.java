package com.sap.sailing.domain.common;

import java.io.Serializable;
import java.util.List;

import com.sap.sse.common.Util.Pair;

public interface PolarSheetsXYDiagramData extends Serializable {
    
    List<Pair<Double, Double>> getPointsForUpwindStarboardAverageSpeed();
    
    List<Pair<Double, Double>> getPointsForUpwindStarboardAverageAngle();

    List<Pair<Double, Double>> getPointsForUpwindStarboardAverageAngleMovingAverage();

    List<Pair<Double, Double>> getPointsForUpwindStarboardAverageSpeedMovingAverage();

    List<Pair<Double, Double>> getPointsForUpwindStarboardAverageConfidence();

}
