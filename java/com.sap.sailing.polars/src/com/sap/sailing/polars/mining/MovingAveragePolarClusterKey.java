package com.sap.sailing.polars.mining;

import com.sap.sailing.domain.common.Speed;
import com.sap.sse.datamining.annotations.Dimension;
import com.sap.sse.datamining.data.Cluster;

public interface MovingAveragePolarClusterKey extends LegTypePolarClusterKey {

    /**
     * 
     * @return wind speed cluster
     */
    @Dimension(messageKey = "windSpeed")
    Cluster<Speed> getWindSpeedCluster();

}