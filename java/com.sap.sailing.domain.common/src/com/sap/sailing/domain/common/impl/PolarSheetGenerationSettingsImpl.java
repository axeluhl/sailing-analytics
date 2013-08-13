package com.sap.sailing.domain.common.impl;

import com.sap.sailing.domain.common.PolarSheetGenerationSettings;

public class PolarSheetGenerationSettingsImpl implements PolarSheetGenerationSettings {
    
    public static PolarSheetGenerationSettings createStandardPolarSettings() {
        return new PolarSheetGenerationSettingsImpl(200, 0.1, 10, 20, 0.5, true, true, 3);
    }

    private static final long serialVersionUID = 2731616509404813790L;
    private Integer minimumDataCountPerGraph;
    private double minimumWindConfidence;
    private Integer minimumDataCountPerAngle;
    private Integer numberOfHistogramColumns;
    private double minimumConfidenceMeasure;
    private boolean useOnlyWindGaugesForWindSpeed;
    private double outlierDetectionFactor;
    private boolean shouldRemoveOutliers;
    
    //GWT
    PolarSheetGenerationSettingsImpl() {};

    public PolarSheetGenerationSettingsImpl(Integer minimumDataCountPerGraph, double minimumWindConfidence,
            Integer minimumDataCountPerAngle, Integer numberOfHistogramColumns, double minimumConfidenceMeasure,
            boolean useOnlyWindGaugesForWindSpeed, boolean shouldRemoveOutliers, double outlierDetectionFactor) {
        this.minimumDataCountPerGraph = minimumDataCountPerGraph;
        this.minimumWindConfidence = minimumWindConfidence;
        this.minimumDataCountPerAngle = minimumDataCountPerAngle;
        this.numberOfHistogramColumns = numberOfHistogramColumns;
        this.minimumConfidenceMeasure = minimumConfidenceMeasure;
        this.useOnlyWindGaugesForWindSpeed = useOnlyWindGaugesForWindSpeed;
        this.shouldRemoveOutliers = shouldRemoveOutliers;
        this.outlierDetectionFactor = outlierDetectionFactor;
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
    public double getOutlierDetectionFactor() {
        return outlierDetectionFactor;
    }


}
