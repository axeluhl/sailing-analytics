package com.sap.sailing.polars.mining;

import com.sap.sse.common.Bearing;
import com.sap.sse.datamining.annotations.Dimension;
import com.sap.sse.datamining.data.Cluster;

/**
 * Allows to group the incoming fixes by beat angle range.
 * 
 * @author D054528 (Frederik Petersen)
 *
 */
public interface AngleClusterPolarClusterKey extends BasePolarClusterKey {

    @Dimension(messageKey = "angleCluster")
    Cluster<Bearing> getAngleCluster();

}
