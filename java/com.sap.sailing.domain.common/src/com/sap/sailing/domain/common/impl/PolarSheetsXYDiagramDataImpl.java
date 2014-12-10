package com.sap.sailing.domain.common.impl;

import java.util.List;

import com.sap.sailing.domain.common.PolarSheetsXYDiagramData;
import com.sap.sse.common.Util.Pair;

public class PolarSheetsXYDiagramDataImpl implements PolarSheetsXYDiagramData {
    
    private static final long serialVersionUID = 778667444303004468L;

    PolarSheetsXYDiagramDataImpl() {}

    private List<Pair<Double, Double>> pointsForUpwindStarboardAverageAngle;
    private List<Pair<Double, Double>> pointsForUpwindStarboardAverageSpeed;
    private List<Pair<Double, Double>> pointsForUpwindStarboardAverageAngleMovingAverage;
    private List<Pair<Double, Double>> pointsForUpwindStarboardAverageSpeedMovingAverage;
    private List<Pair<Double, Double>> pointsForUpwindStarboardAverageConfidence;

    public PolarSheetsXYDiagramDataImpl(List<Pair<Double, Double>> pointsForUpwindStarboardAverageAngle,
            List<Pair<Double, Double>> pointsForUpwindStarboardAverageSpeed,
            List<Pair<Double, Double>> pointsForUpwindStarboardAverageAngleMovingAverage,
            List<Pair<Double, Double>> pointsForUpwindStarboardAverageSpeedMovingAverage,
            List<Pair<Double, Double>> pointsForUpwindStarboardAverageConfidence) {
        this.pointsForUpwindStarboardAverageAngle = pointsForUpwindStarboardAverageAngle;
        this.pointsForUpwindStarboardAverageSpeed = pointsForUpwindStarboardAverageSpeed;
        this.pointsForUpwindStarboardAverageAngleMovingAverage = pointsForUpwindStarboardAverageAngleMovingAverage;
        this.pointsForUpwindStarboardAverageSpeedMovingAverage = pointsForUpwindStarboardAverageSpeedMovingAverage;
        this.pointsForUpwindStarboardAverageConfidence = pointsForUpwindStarboardAverageConfidence;
    }

    @Override
    public List<Pair<Double, Double>> getPointsForUpwindStarboardAverageAngle() {
        return pointsForUpwindStarboardAverageAngle;
    }

    @Override
    public List<Pair<Double, Double>> getPointsForUpwindStarboardAverageSpeed() {
        return pointsForUpwindStarboardAverageSpeed;
    }

    @Override
    public List<Pair<Double, Double>> getPointsForUpwindStarboardAverageAngleMovingAverage() {
        return pointsForUpwindStarboardAverageAngleMovingAverage;
    }

    @Override
    public List<Pair<Double, Double>> getPointsForUpwindStarboardAverageSpeedMovingAverage() {
        return pointsForUpwindStarboardAverageSpeedMovingAverage;
    }

    @Override
    public List<Pair<Double, Double>> getPointsForUpwindStarboardAverageConfidence() {
        return pointsForUpwindStarboardAverageConfidence;
    }

}
