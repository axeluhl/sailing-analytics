package com.sap.sailing.polars.mining;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.domain.common.Speed;
import com.sap.sse.datamining.data.Cluster;
import com.sap.sse.datamining.data.ClusterGroup;

public class AverageAngleContainer {

    private ClusterGroup<Speed> speedClusterGroup;
    
    private static class DataCountAngleSumAndAverage {
        private int dataCount;
        private double angleSumDeg;
        private double averageAngleDeg;
        protected DataCountAngleSumAndAverage(int dataCount, double angleSumDeg, double averageAngleDeg) {
            super();
            this.dataCount = dataCount;
            this.angleSumDeg = angleSumDeg;
            this.averageAngleDeg = averageAngleDeg;
        }
        public double getAverageAngleDeg() {
            return averageAngleDeg;
        }
        public synchronized void add(double angleRad) {
            dataCount++;
            angleSumDeg += angleRad;
            averageAngleDeg = angleSumDeg / (double) dataCount;
        }
    }
    
    private Map<BoatClassMasterdata, Map<Cluster<Speed>, DataCountAngleSumAndAverage>> dataCountAndAngleSumMap;

    public AverageAngleContainer(ClusterGroup<Speed> speedClusterGroup) {
        this.speedClusterGroup = speedClusterGroup;
        dataCountAndAngleSumMap = new HashMap<>();
    }

    public void addFix(BoatClassMasterdata boatClassMasterdata, Speed windSpeed, int roundedAngleDeg) {
        Cluster<Speed> windCluster = speedClusterGroup.getClusterFor(windSpeed);
        if (!dataCountAndAngleSumMap.containsKey(boatClassMasterdata)) {
            dataCountAndAngleSumMap.put(boatClassMasterdata, new HashMap<>());
        }
        Map<Cluster<Speed>, DataCountAngleSumAndAverage> boatClassMap = dataCountAndAngleSumMap.get(boatClassMasterdata);
        DataCountAngleSumAndAverage triple = boatClassMap.get(windCluster);
        if (triple == null) {
            triple = createInitialTriple();
            boatClassMap.put(windCluster, triple);
        }
        triple.add(roundedAngleDeg);
    }

    private DataCountAngleSumAndAverage createInitialTriple() {
        return new DataCountAngleSumAndAverage(0, 0.0, 0.0);
    }
    
    double getAverageAngleDeg(BoatClassMasterdata boatClassMasterdata, Speed windSpeed) {
        Double result = null;
        if (dataCountAndAngleSumMap.containsKey(boatClassMasterdata)) {
            Map<Cluster<Speed>, DataCountAngleSumAndAverage> boatClassMap = dataCountAndAngleSumMap.get(boatClassMasterdata);
            Cluster<Speed> windCluster = speedClusterGroup.getClusterFor(windSpeed);
            if(boatClassMap.containsKey(windCluster)) {
                DataCountAngleSumAndAverage triple = boatClassMap.get(windCluster);
                result = triple.getAverageAngleDeg();
            }
        }
        return result;
    }

}
