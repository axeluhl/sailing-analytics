package com.sap.sailing.datamining.impl;


import com.sap.sailing.datamining.WindStrengthCluster;
import com.sap.sse.datamining.impl.data.deprecated.ClusterOfComparableImpl;

public class WindStrengthClusterImpl extends ClusterOfComparableImpl<Double> implements WindStrengthCluster {

    public WindStrengthClusterImpl(String name, Double lowerRangeInBeaufort, Double upperRangeInBeaufort) {
        super(name, upperRangeInBeaufort, lowerRangeInBeaufort);
    }

}
