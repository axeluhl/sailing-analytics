package com.sap.sailing.domain.common.impl;

import com.sap.sailing.domain.common.PolarSheetGenerationSettings;

public class PolarSheetGenerationSettingsImpl implements PolarSheetGenerationSettings {

    private static final long serialVersionUID = 2731616509404813790L;
    private Integer minimumDataCountPerGraph;
    private double minimumWindConfidence;
    private Integer minimumDataCountPerAngle;
    private Integer numberOfHistogramColumns;
    private double minimumConfidenceMeasure;
    
    //GWT
    PolarSheetGenerationSettingsImpl() {};

    public PolarSheetGenerationSettingsImpl(Integer minimumDataCountPerGraph, double minimumWindConfidence,
            Integer minimumDataCountPerAngle, Integer numberOfHistogramColumns, double minimumConfidenceMeasure) {
        this.minimumDataCountPerGraph = minimumDataCountPerGraph;
        this.minimumWindConfidence = minimumWindConfidence;
        this.minimumDataCountPerAngle = minimumDataCountPerAngle;
        this.numberOfHistogramColumns = numberOfHistogramColumns;
        this.minimumConfidenceMeasure = minimumConfidenceMeasure;
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


}
