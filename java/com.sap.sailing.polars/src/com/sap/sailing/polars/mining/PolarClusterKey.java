package com.sap.sailing.polars.mining;

import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.domain.common.Speed;
import com.sap.sse.datamining.data.Cluster;
import com.sap.sse.datamining.shared.annotations.Dimension;

public interface PolarClusterKey {

    /**
     * 
     * @return degrees boat to windDirection in DEG
     */
    @Dimension(messageKey = "angleToWind")
    RoundedAngleToTheWind getRoundedAngleToTheWind();

    /**
     * 
     * @return wind speed cluster
     */
    @Dimension(messageKey = "windSpeed")
    Cluster<Speed> getWindSpeedCluster();

    @Dimension(messageKey = "boatClass")
    BoatClassMasterdata getBoatClassMasterData();

}