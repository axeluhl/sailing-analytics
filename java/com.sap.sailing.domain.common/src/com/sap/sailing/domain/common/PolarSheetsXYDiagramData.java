package com.sap.sailing.domain.common;

import java.io.Serializable;
import java.util.List;

import com.sap.sse.common.Util.Pair;

public interface PolarSheetsXYDiagramData extends Serializable {

    List<Pair<Double, Double>> getPointsForUpwindStarboardAverageSpeedMovingAverage();

    List<Pair<Double, Double>> getPointsForUpwindStarboardAverageConfidence();

}
