package com.sap.sailing.domain.common.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.common.PolarSheetGenerationSettings;

public class PolarSheetGenerationSettingsImpl implements PolarSheetGenerationSettings {
    
    public static PolarSheetGenerationSettings createStandardPolarSettings() {
        double[] levels = { 4., 6., 8., 10., 12., 14., 16., 20., 25., 30. };
        WindSpeedSteppingWithMaxDistance windStepping = new WindSpeedSteppingWithMaxDistance(levels, 2.5);
        return new PolarSheetGenerationSettingsImpl(50, 0.1, 20, 20, 0.1, true, true, 2, 0.05, true, windStepping,
                false, 0);
    }
    
    public static PolarSheetGenerationSettings createBackendPolarSettings() {
        List<Double> levelList = new ArrayList<Double>();
        for (double levelValue = 0.5; levelValue < 35; levelValue = levelValue + 0.5) {
            levelList.add(levelValue);
        }
        double[] levels = new double[levelList.size()];
        int i=0;
        for (Double level : levelList) {
            levels[i++] = level;
        }
        WindSpeedSteppingWithMaxDistance windStepping = new WindSpeedSteppingWithMaxDistance(levels, 0.5);
        return new PolarSheetGenerationSettingsImpl(50, 0.1, 20, 20, 0.1, true, true, 2, 0.05, true, windStepping,
                false, 1);
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
    private WindSpeedSteppingWithMaxDistance windStepping;
    private boolean splitByWindGauges;
    private int numberOfLeadingCompetitorsToInclude;
    
    //GWT
    PolarSheetGenerationSettingsImpl() {};

    public PolarSheetGenerationSettingsImpl(Integer minimumDataCountPerGraph, double minimumWindConfidence,
            Integer minimumDataCountPerAngle, Integer numberOfHistogramColumns, double minimumConfidenceMeasure,
            boolean useOnlyWindGaugesForWindSpeed, boolean shouldRemoveOutliers,
            double outlierDetectionNeighboorhoodRadius, double outlierMinimumNeighboorhoodPct,
            boolean useOnlyEstimationForWindDirection, WindSpeedSteppingWithMaxDistance windStepping,
            boolean splitByWindGauges, int numberOfLeadingCompetitorsToInclude) {
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
        this.numberOfLeadingCompetitorsToInclude = numberOfLeadingCompetitorsToInclude;
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
    public WindSpeedSteppingWithMaxDistance getWindSpeedStepping() {
        return windStepping;
    }

    @Override
    public boolean splitByWindgauges() {
        return splitByWindGauges;
    }

    @Override
    public boolean areDefault() {
        return createStandardPolarSettings().equals(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(minimumConfidenceMeasure);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((minimumDataCountPerAngle == null) ? 0 : minimumDataCountPerAngle.hashCode());
        result = prime * result + ((minimumDataCountPerGraph == null) ? 0 : minimumDataCountPerGraph.hashCode());
        temp = Double.doubleToLongBits(minimumWindConfidence);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((numberOfHistogramColumns == null) ? 0 : numberOfHistogramColumns.hashCode());
        temp = Double.doubleToLongBits(outlierDetectionNeighboorhoodRadius);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(outlierMinimumNeighboorhoodPct);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + (shouldRemoveOutliers ? 1231 : 1237);
        result = prime * result + (splitByWindGauges ? 1231 : 1237);
        result = prime * result + (useOnlyEstimationForWindDirection ? 1231 : 1237);
        result = prime * result + (useOnlyWindGaugesForWindSpeed ? 1231 : 1237);
        result = prime * result + ((windStepping == null) ? 0 : windStepping.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PolarSheetGenerationSettingsImpl other = (PolarSheetGenerationSettingsImpl) obj;
        if (Double.doubleToLongBits(minimumConfidenceMeasure) != Double
                .doubleToLongBits(other.minimumConfidenceMeasure))
            return false;
        if (minimumDataCountPerAngle == null) {
            if (other.minimumDataCountPerAngle != null)
                return false;
        } else if (!minimumDataCountPerAngle.equals(other.minimumDataCountPerAngle))
            return false;
        if (minimumDataCountPerGraph == null) {
            if (other.minimumDataCountPerGraph != null)
                return false;
        } else if (!minimumDataCountPerGraph.equals(other.minimumDataCountPerGraph))
            return false;
        if (Double.doubleToLongBits(minimumWindConfidence) != Double.doubleToLongBits(other.minimumWindConfidence))
            return false;
        if (numberOfHistogramColumns == null) {
            if (other.numberOfHistogramColumns != null)
                return false;
        } else if (!numberOfHistogramColumns.equals(other.numberOfHistogramColumns))
            return false;
        if (Double.doubleToLongBits(outlierDetectionNeighboorhoodRadius) != Double
                .doubleToLongBits(other.outlierDetectionNeighboorhoodRadius))
            return false;
        if (Double.doubleToLongBits(outlierMinimumNeighboorhoodPct) != Double
                .doubleToLongBits(other.outlierMinimumNeighboorhoodPct))
            return false;
        if (shouldRemoveOutliers != other.shouldRemoveOutliers)
            return false;
        if (splitByWindGauges != other.splitByWindGauges)
            return false;
        if (useOnlyEstimationForWindDirection != other.useOnlyEstimationForWindDirection)
            return false;
        if (useOnlyWindGaugesForWindSpeed != other.useOnlyWindGaugesForWindSpeed)
            return false;
        if (windStepping == null) {
            if (other.windStepping != null)
                return false;
        } else if (!windStepping.equals(other.windStepping))
            return false;
        return true;
    }

    @Override
    public int getNumberOfLeadingCompetitorsToInclude() {
        return numberOfLeadingCompetitorsToInclude;
    }


}
