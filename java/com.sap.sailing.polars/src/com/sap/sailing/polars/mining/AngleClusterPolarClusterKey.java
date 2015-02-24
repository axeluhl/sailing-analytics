package com.sap.sailing.polars.mining;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sse.datamining.data.Cluster;
import com.sap.sse.datamining.shared.annotations.Dimension;

public interface AngleClusterPolarClusterKey extends BasePolarClusterKey {
    
    @Dimension(messageKey = "angleDiffTrueWindToBoat")
    Cluster<Bearing> getAngleCluster();

}
