package com.sap.sailing.domain.common;

import com.sap.sailing.domain.common.impl.WindSpeedSteppingWithMaxDistance;
import com.sap.sse.common.settings.SerializableSettings;

public abstract class PolarSheetGenerationSettings extends SerializableSettings {

    private static final long serialVersionUID = 2018523638816898236L;

    public abstract Integer getMinimumDataCountPerGraph();

    public abstract double getMinimumWindConfidence();

    public abstract Integer getMinimumDataCountPerAngle();

    public abstract int getNumberOfHistogramColumns();

    public abstract double getMinimumConfidenceMeasure();

    public abstract boolean useOnlyWindGaugesForWindSpeed();

    public abstract boolean shouldRemoveOutliers();

    public abstract double getOutlierDetectionNeighborhoodRadius();

    public abstract double getOutlierMinimumNeighborhoodPct();

    public abstract boolean useOnlyEstimatedForWindDirection();

    public abstract WindSpeedSteppingWithMaxDistance getWindSpeedStepping();

    public abstract boolean splitByWindgauges();

    public abstract boolean areDefault();

    public abstract double getPctOfLeadingCompetitorsToInclude();

}
