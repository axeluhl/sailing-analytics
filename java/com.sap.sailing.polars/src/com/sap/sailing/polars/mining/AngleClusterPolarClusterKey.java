package com.sap.sailing.polars.mining;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sse.datamining.annotations.Dimension;
import com.sap.sse.datamining.data.Cluster;

public interface AngleClusterPolarClusterKey extends BasePolarClusterKey {

    @Dimension(messageKey = "angleCluster")
    Cluster<Bearing> getAngleCluster();

}
