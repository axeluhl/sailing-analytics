package com.sap.sailing.domain.common.impl;

import com.sap.sailing.domain.common.PolarSheetGenerationSettings;

public class PolarSheetGenerationSettingsImpl implements PolarSheetGenerationSettings {
    
    public static PolarSheetGenerationSettings createStandardPolarSettings() {
        Integer[] levels = { 4, 6, 8, 10, 12, 14, 16, 20, 25, 30 };
        WindSteppingWithMaxDistance windStepping = new WindSteppingWithMaxDistance(levels, 2.0);
        return new PolarSheetGenerationSettingsImpl(200, 0.1, 10, 20, 0.1, true, true, 2, 0.05, true, windStepping,
                false);
    }

    private static final long serialVersionUID = 2731616509404813790L;
    private Integer minimumDataCountPerGraph;
    private double minimumWindConfidence;
    private Integer minimumDataCountPerAngle;
    private Integer numberOfHistogramColumns;
    private double minimumConfidenceMeasure;
    private boolean useOnlyWindGaugesForWindSpeed;
    private boolean shouldRemoveOutliers;
    private double outlierDetectionNeighboorhoodRadius;
    private double outlierMinimumNeighboorhoodPct;
    private boolean useOnlyEstimationForWindDirection;
    private WindSteppingWithMaxDistance windStepping;
    private boolean splitByWindGauges;
    
    //GWT
    PolarSheetGenerationSettingsImpl() {};

    public PolarSheetGenerationSettingsImpl(Integer minimumDataCountPerGraph, double minimumWindConfidence,
            Integer minimumDataCountPerAngle, Integer numberOfHistogramColumns, double minimumConfidenceMeasure,
            boolean useOnlyWindGaugesForWindSpeed, boolean shouldRemoveOutliers,
            double outlierDetectionNeighboorhoodRadius, double outlierMinimumNeighboorhoodPct,
            boolean useOnlyEstimationForWindDirection, WindSteppingWithMaxDistance windStepping,
            boolean splitByWindGauges) {
        this.minimumDataCountPerGraph = minimumDataCountPerGraph;
        this.minimumWindConfidence = minimumWindConfidence;
        this.minimumDataCountPerAngle = minimumDataCountPerAngle;
        this.numberOfHistogramColumns = numberOfHistogramColumns;
        this.minimumConfidenceMeasure = minimumConfidenceMeasure;
        this.useOnlyWindGaugesForWindSpeed = useOnlyWindGaugesForWindSpeed;
        this.shouldRemoveOutliers = shouldRemoveOutliers;
        this.outlierDetectionNeighboorhoodRadius = outlierDetectionNeighboorhoodRadius;
        this.outlierMinimumNeighboorhoodPct = outlierMinimumNeighboorhoodPct;
        this.useOnlyEstimationForWindDirection = useOnlyEstimationForWindDirection;
        this.windStepping = windStepping;
        this.splitByWindGauges = splitByWindGauges;
    }

    @Override
    public Integer getMinimumDataCountPerGraph() {
        return minimumDataCountPerGraph;
    }

    @Override
    public double getMinimumWindConfidence() {
        return minimumWindConfidence;
    }

    @Override
    public Integer getMinimumDataCountPerAngle() {
        return minimumDataCountPerAngle;
    }

    @Override
    public int getNumberOfHistogramColumns() {
        return numberOfHistogramColumns;
    }

    @Override
    public double getMinimumConfidenceMeasure() {
        return minimumConfidenceMeasure;
    }

    @Override
    public boolean useOnlyWindGaugesForWindSpeed() {
        return useOnlyWindGaugesForWindSpeed;
    }

    @Override
    public boolean shouldRemoveOutliers() {
        return shouldRemoveOutliers;
    }

    @Override
    public double getOutlierDetectionNeighborhoodRadius() {
        return outlierDetectionNeighboorhoodRadius;
    }

    @Override
    public double getOutlierMinimumNeighborhoodPct() {
        return outlierMinimumNeighboorhoodPct;
    }

    @Override
    public boolean useOnlyEstimatedForWindDirection() {
        return useOnlyEstimationForWindDirection;
    }

    @Override
    public WindSteppingWithMaxDistance getWindStepping() {
        return windStepping;
    }

    @Override
    public boolean splitByWindgauges() {
        return splitByWindGauges;
    }

}
