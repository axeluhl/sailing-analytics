package com.sap.sailing.polars.datamining.shared;

import java.io.Serializable;

import com.sap.sailing.domain.common.PolarSheetGenerationSettings;

public interface PolarAggregation extends Serializable {

    void addElement(PolarStatistic dataEntry);

    double[] getAverageSpeedsPerAngle();
    
    int[] getCountPerAngle();
    
    int getCount();
    
    PolarSheetGenerationSettings getSettings();
}
