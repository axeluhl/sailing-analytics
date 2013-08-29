package com.sap.sailing.domain.common;

import java.io.Serializable;

import com.sap.sailing.domain.common.impl.PolarSheetsWindStepping;

public interface PolarSheetGenerationSettings extends Serializable{

    Integer getMinimumDataCountPerGraph();

    double getMinimumWindConfidence();

    Integer getMinimumDataCountPerAngle();

    int getNumberOfHistogramColumns();

    double getMinimumConfidenceMeasure();

    boolean useOnlyWindGaugesForWindSpeed();

    boolean shouldRemoveOutliers();

    double getOutlierDetectionNeighborhoodRadius();

    double getOutlierMinimumNeighborhoodPct();

    boolean useOnlyEstimatedForWindDirection();

    PolarSheetsWindStepping getWindStepping();

}
