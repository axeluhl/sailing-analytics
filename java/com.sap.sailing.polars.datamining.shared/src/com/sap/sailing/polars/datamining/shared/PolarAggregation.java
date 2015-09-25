package com.sap.sailing.polars.datamining.shared;

import java.io.Serializable;
import java.util.Map;

public interface PolarAggregation extends Serializable {

    void addElement(PolarStatistic dataEntry);

    double[] getAverageSpeedsPerAngle();
    
    int[] getCountPerAngle();
    
    int getCount();
    
    PolarDataMiningSettings getSettings();
    
    Map<Integer, Map<Double, Integer>> getCountHistogramPerAngle();
    
}
