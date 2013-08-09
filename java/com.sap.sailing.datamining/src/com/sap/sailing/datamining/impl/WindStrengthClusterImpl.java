package com.sap.sailing.datamining.impl;


import com.sap.sailing.datamining.WindStrengthCluster;

public class WindStrengthClusterImpl extends ClusterOfComparableImpl<Double> implements WindStrengthCluster {

    public WindStrengthClusterImpl(String name, Double upperRangeInBeaufort, Double lowerRangeInBeaufort) {
        super(name, upperRangeInBeaufort, lowerRangeInBeaufort);
    }

}
