package com.sap.sailing.polars.mining;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.SpeedWithBearing;
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
    
    private Map<BoatClassMasterdata, Map<Cluster<Speed>, DataCountAngleSumAndAverage>> dataCountAndAngleSumMap;

    public AverageAngleContainer(ClusterGroup<Speed> speedClusterGroup) {
        this.speedClusterGroup = speedClusterGroup;
        dataCountAndAngleSumMap = new HashMap<>();
    }
    
    /**
     * Looks at all entries in {@link #dataCountAndAngleSumMap} and determines the wind speed and true wind angle at
     * which the boat has most likely been sailing to achieve the <code>boatSpeed</code> provided.
     */
    SpeedWithBearing getAverageTrueWindSpeedAndAngle(BoatClassMasterdata boatClassMasterData, Speed boatSpeed) {
        // TODO implement getAverageTrueWindSpeedAndAngle
        return null;
    }

    public void addFix(BoatClassMasterdata boatClassMasterdata, Speed windSpeed, int roundedAngleDeg) {
        Cluster<Speed> windCluster = speedClusterGroup.getClusterFor(windSpeed);
        if (!dataCountAndAngleSumMap.containsKey(boatClassMasterdata)) {
            dataCountAndAngleSumMap.put(boatClassMasterdata, new HashMap<>());
        }
        Map<Cluster<Speed>, DataCountAngleSumAndAverage> boatClassMap = dataCountAndAngleSumMap.get(boatClassMasterdata);
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
    Double getAverageAngleDeg(BoatClassMasterdata boatClassMasterdata, Speed windSpeed) {
        Double result = null;
        if (dataCountAndAngleSumMap.containsKey(boatClassMasterdata)) {
            Map<Cluster<Speed>, DataCountAngleSumAndAverage> boatClassMap = dataCountAndAngleSumMap.get(boatClassMasterdata);
            Cluster<Speed> windCluster = speedClusterGroup.getClusterFor(windSpeed);
            if (boatClassMap.containsKey(windCluster)) {
                DataCountAngleSumAndAverage triple = boatClassMap.get(windCluster);
                result = triple.getAverageAngleDeg();
            }
        }
        return result;
    }

}
