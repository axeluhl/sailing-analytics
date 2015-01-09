package com.sap.sailing.polars.mining;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.Speed;
import com.sap.sse.datamining.data.Cluster;
import com.sap.sse.datamining.data.ClusterGroup;

public class AverageAngleContainer {

    private ClusterGroup<Speed> speedClusterGroup;
    
    private static class DataCountAngleSumAndAverage {
        private int dataCount;
        private double angleSumDeg;
        private double averageAngleDeg;
        protected DataCountAngleSumAndAverage() {
            super();
            this.dataCount = 0;
            this.angleSumDeg = 0.0;
            this.averageAngleDeg = 0.0;
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
    
    private Map<BoatClass, Map<Cluster<Speed>, DataCountAngleSumAndAverage>> dataCountAndAngleSumMap;

    public AverageAngleContainer(ClusterGroup<Speed> speedClusterGroup) {
        this.speedClusterGroup = speedClusterGroup;
        dataCountAndAngleSumMap = new HashMap<>();
    }

    public void addFix(BoatClass boatClass, Speed windSpeed, int roundedAngleDeg) {
        Cluster<Speed> windCluster = speedClusterGroup.getClusterFor(windSpeed);
        if (!dataCountAndAngleSumMap.containsKey(boatClass)) {
            dataCountAndAngleSumMap.put(boatClass, new HashMap<>());
        }
        Map<Cluster<Speed>, DataCountAngleSumAndAverage> boatClassMap = dataCountAndAngleSumMap.get(boatClass);
        DataCountAngleSumAndAverage triple = boatClassMap.get(windCluster);
        if (triple == null) {
            triple = new DataCountAngleSumAndAverage();
            boatClassMap.put(windCluster, triple);
        }
        triple.add(roundedAngleDeg);
    }

    /**
     * @return <code>null</code> if no entry exists for the <code>windSpeed</code> requested; a valid angle in degrees
     *         otherwise, representing the average angle of this cluster
     */
    Double getAverageAngleDeg(BoatClass boatClass, Speed windSpeed) {
        Double result = null;
        if (dataCountAndAngleSumMap.containsKey(boatClass)) {
            Map<Cluster<Speed>, DataCountAngleSumAndAverage> boatClassMap = dataCountAndAngleSumMap.get(boatClass);
            Cluster<Speed> windCluster = speedClusterGroup.getClusterFor(windSpeed);
            if (boatClassMap.containsKey(windCluster)) {
                DataCountAngleSumAndAverage triple = boatClassMap.get(windCluster);
                result = triple.getAverageAngleDeg();
            }
        }
        return result;
    }

}
