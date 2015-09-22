package com.sap.sailing.polars.datamining.shared;

import java.io.Serializable;

public interface PolarAggregation extends Serializable {

    void addElement(PolarStatistic dataEntry);

    double[] getAverageSpeedsPerAngle();
    
    int[] getCountPerAngle();
    
    int getCount();
    
    PolarDataMiningSettings getSettings();
    
    int[][] getCountHistogramPerAngle();
}
