package com.sap.sailing.domain.common;

import java.io.Serializable;

import com.sap.sailing.domain.common.impl.WindSpeedSteppingWithMaxDistance;
import com.sap.sailing.domain.common.settings.Settings;

public interface PolarSheetGenerationSettings extends Serializable, Settings {

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

    WindSpeedSteppingWithMaxDistance getWindSpeedStepping();

    boolean splitByWindgauges();

    boolean areDefault();

    double getPctOfLeadingCompetitorsToInclude();

}
