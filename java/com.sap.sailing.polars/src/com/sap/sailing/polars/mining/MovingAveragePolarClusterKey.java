package com.sap.sailing.polars.mining;

import com.sap.sailing.domain.common.Speed;
import com.sap.sse.datamining.data.Cluster;
import com.sap.sse.datamining.shared.annotations.Dimension;

public interface MovingAveragePolarClusterKey extends TackAndLegTypePolarClusterKey {

    /**
     * 
     * @return wind speed cluster
     */
    @Dimension(messageKey = "windSpeed")
    Cluster<Speed> getWindSpeedCluster();

}