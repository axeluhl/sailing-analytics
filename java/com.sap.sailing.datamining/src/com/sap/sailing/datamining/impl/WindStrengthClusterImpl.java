package com.sap.sailing.datamining.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.sap.sailing.datamining.WindStrengthCluster;

public class WindStrengthClusterImpl extends ClusterOfComparableImpl<Double> implements WindStrengthCluster {

    public static WindStrengthCluster VeryLight = new WindStrengthClusterImpl("Very Light", 2.5, 0.0);
    public static WindStrengthCluster Light = new WindStrengthClusterImpl("Very Light", 5.0, 2.5);
    public static WindStrengthCluster Medium = new WindStrengthClusterImpl("Very Light", 7.5, 5.0);
    public static WindStrengthCluster Strong = new WindStrengthClusterImpl("Very Light", 10.0, 7.5);
    public static WindStrengthCluster VeryStrong = new WindStrengthClusterImpl("Very Light", 12.0, 10.0);
    public static Collection<WindStrengthCluster> StandardClusters = Arrays.asList(VeryLight, Light, Medium, Strong, VeryStrong);

    public WindStrengthClusterImpl(String name, Double upperRangeInBeaufort, Double lowerRangeInBeaufort) {
        super(name, upperRangeInBeaufort, lowerRangeInBeaufort);
    }

    public static WindStrengthCluster getClusterFor(double windStrengthInBeafort, Collection<WindStrengthCluster> clusters) {
        List<WindStrengthCluster> sortedClusters = new ArrayList<WindStrengthCluster>(clusters);
        Collections.sort(sortedClusters, new Comparator<WindStrengthCluster>() {
            @Override
            public int compare(WindStrengthCluster cluster1, WindStrengthCluster cluster2) {
                return cluster1.getUpperRange().compareTo(cluster2.getUpperRange());
            }
        });
        
        for (WindStrengthCluster cluster : sortedClusters) {
            if (cluster.isInRange(windStrengthInBeafort)) {
                return cluster;
            }
        }
        return null;
    }

}
