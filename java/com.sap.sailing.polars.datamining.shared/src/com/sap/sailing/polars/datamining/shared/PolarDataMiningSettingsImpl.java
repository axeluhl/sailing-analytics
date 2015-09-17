package com.sap.sailing.polars.datamining.shared;

import com.sap.sailing.domain.common.impl.WindSpeedSteppingWithMaxDistance;

public class PolarDataMiningSettingsImpl extends PolarDataMiningSettings {

    public static PolarDataMiningSettingsImpl createStandardPolarSettings() {
        double[] levels = { 4., 6., 8., 10., 12., 14., 16., 20., 25., 30. };
        WindSpeedSteppingWithMaxDistance windStepping = new WindSpeedSteppingWithMaxDistance(levels, 2.5);
        return new PolarDataMiningSettingsImpl(50, 0.01, 20, 20, true, true, windStepping);
    }

    private static final long serialVersionUID = 2731616509404813790L;
    private Integer minimumDataCountPerGraph;
    private double minimumWindConfidence;
    private Integer minimumDataCountPerAngle;
    private Integer numberOfHistogramColumns;
    private boolean useOnlyWindGaugesForWindSpeed;
    private boolean useOnlyEstimationForWindDirection;
    private WindSpeedSteppingWithMaxDistance windStepping;
    
    //GWT
    PolarDataMiningSettingsImpl() {};

    public PolarDataMiningSettingsImpl(Integer minimumDataCountPerGraph, double minimumWindConfidence,
            Integer minimumDataCountPerAngle, Integer numberOfHistogramColumns,
            boolean useOnlyWindGaugesForWindSpeed,
            boolean useOnlyEstimationForWindDirection, WindSpeedSteppingWithMaxDistance windStepping) {
        this.minimumDataCountPerGraph = minimumDataCountPerGraph;
        this.minimumWindConfidence = minimumWindConfidence;
        this.minimumDataCountPerAngle = minimumDataCountPerAngle;
        this.numberOfHistogramColumns = numberOfHistogramColumns;
        this.useOnlyWindGaugesForWindSpeed = useOnlyWindGaugesForWindSpeed;
        this.useOnlyEstimationForWindDirection = useOnlyEstimationForWindDirection;
        this.windStepping = windStepping;
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
    public boolean useOnlyWindGaugesForWindSpeed() {
        return useOnlyWindGaugesForWindSpeed;
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
    public boolean areDefault() {
        return createStandardPolarSettings().equals(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((minimumDataCountPerAngle == null) ? 0 : minimumDataCountPerAngle.hashCode());
        result = prime * result + ((minimumDataCountPerGraph == null) ? 0 : minimumDataCountPerGraph.hashCode());
        long temp;
        temp = Double.doubleToLongBits(minimumWindConfidence);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((numberOfHistogramColumns == null) ? 0 : numberOfHistogramColumns.hashCode());
        result = prime * result + (useOnlyEstimationForWindDirection ? 1231 : 1237);
        result = prime * result + (useOnlyWindGaugesForWindSpeed ? 1231 : 1237);
        result = prime * result + ((windStepping == null) ? 0 : windStepping.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        PolarDataMiningSettingsImpl other = (PolarDataMiningSettingsImpl) obj;
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
    
    

}
