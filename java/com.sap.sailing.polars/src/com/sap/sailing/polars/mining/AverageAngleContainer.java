package com.sap.sailing.polars.mining;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.domain.common.Speed;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.datamining.data.Cluster;
import com.sap.sse.datamining.data.ClusterGroup;

public class AverageAngleContainer {

    private ClusterGroup<Speed> speedClusterGroup;
    
    private Map<BoatClassMasterdata, Map<Cluster<Speed>, Triple<Integer, Double, Double>>> dataCountAndAngleSumMap;

    public AverageAngleContainer(ClusterGroup<Speed> speedClusterGroup) {
        this.speedClusterGroup = speedClusterGroup;
        dataCountAndAngleSumMap = new HashMap<>();
    }

    public void addFix(BoatClassMasterdata boatClassMasterdata, Speed windSpeed, int roundedAngle) {
        Cluster<Speed> windCluster = speedClusterGroup.getClusterFor(windSpeed);
        if (!dataCountAndAngleSumMap.containsKey(boatClassMasterdata)) {
            dataCountAndAngleSumMap.put(boatClassMasterdata, new HashMap<>());
        }
        Map<Cluster<Speed>, Triple<Integer, Double, Double>> boatClassMap = dataCountAndAngleSumMap.get(boatClassMasterdata);
        if (!boatClassMap.containsKey(windCluster)) {
            boatClassMap.put(windCluster, createInitialTriple());
        }
        Triple<Integer, Double, Double> triple = boatClassMap.get(windCluster);
        Integer dataCount = triple.getA();
        Double sum = triple.getB();
        dataCount = dataCount + 1;
        sum = sum + roundedAngle;
        double average = sum / (double) dataCount;
        boatClassMap.put(windCluster, new Triple<Integer, Double, Double>(dataCount, sum, average));
    }

    private Triple<Integer, Double, Double> createInitialTriple() {
        return new Triple<Integer, Double, Double>(0, 0.0, 0.0);
    }
    
    Double getAverageAngle(BoatClassMasterdata boatClassMasterdata, Speed windSpeed) {
        Double result = null;
        if (dataCountAndAngleSumMap.containsKey(boatClassMasterdata)) {
            Map<Cluster<Speed>, Triple<Integer, Double, Double>> boatClassMap = dataCountAndAngleSumMap.get(boatClassMasterdata);
            Cluster<Speed> windCluster = speedClusterGroup.getClusterFor(windSpeed);
            if(boatClassMap.containsKey(windCluster)) {
                Triple<Integer, Double, Double> triple = boatClassMap.get(windCluster);
                result = triple.getC();
            }
        }
        return result;
    }

}
