package com.sap.sailing.domain.common;

import java.io.Serializable;
import java.util.List;

import com.sap.sse.common.Util.Pair;

public interface PolarSheetsXYDiagramData extends Serializable {

    List<Pair<Double, Double>> getPointsForAverageSpeedMovingAverage(Tack tack, LegType legType);
    
    List<Pair<Double, Double>> getPointsForAverageConfidence(Tack tack, LegType legType);

}
