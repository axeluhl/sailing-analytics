package com.sap.sailing.domain.common.impl;

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
    
    private Map<Pair<LegType, Tack>, List<Pair<Double, Double>>> pointsForAverageSpeedRegression;

    public PolarSheetsXYDiagramDataImpl(
            Map<Pair<LegType, Tack>, List<Pair<Double, Double>>> regressionSpeedDataLists) {
        pointsForAverageSpeedRegression = regressionSpeedDataLists;
    }

    @Override
    public List<Pair<Double, Double>> getPointsForAverageSpeedRegression(Tack tack, LegType legType) {
        return pointsForAverageSpeedRegression.get(new Pair<LegType, Tack>(legType, tack));
    }
}
