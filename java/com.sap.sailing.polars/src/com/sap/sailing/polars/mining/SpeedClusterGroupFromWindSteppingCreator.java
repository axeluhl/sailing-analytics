package com.sap.sailing.polars.mining;

import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.impl.WindSteppingWithMaxDistance;
import com.sap.sailing.polars.clusters.SpeedClusterGroup;
import com.sap.sse.datamining.data.ClusterGroup;

public class SpeedClusterGroupFromWindSteppingCreator {
    
    public static ClusterGroup<Speed> createSpeedClusterGroupFrom(WindSteppingWithMaxDistance windStepping) {
        double maxDistance = windStepping.getMaxDistance();
        Integer[] rawIntegerStepping = windStepping.getRawStepping();
        double[] rawDoubleLevelMids = new double[rawIntegerStepping.length];
        for (int i = 0; i < rawIntegerStepping.length; i++) {
            rawDoubleLevelMids[i] = rawIntegerStepping[i];
        }
        
        return new SpeedClusterGroup("Speed Cluster Group", rawDoubleLevelMids, maxDistance);
    }

}
