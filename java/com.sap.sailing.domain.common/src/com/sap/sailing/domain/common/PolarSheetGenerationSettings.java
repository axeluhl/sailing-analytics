package com.sap.sailing.domain.common;

import java.io.Serializable;

public interface PolarSheetGenerationSettings extends Serializable{

    Integer getMinimumDataCountPerGraph();

    double getMinimumWindConfidence();

    Integer getMinimumDataCountPerAngle();

    int getNumberOfHistogramColumns();

    double getMinimumConfidenceMeasure();

}
