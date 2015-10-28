package com.sap.sailing.polars.mining;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sse.datamining.annotations.Dimension;

/**
 * 
 * Allows grouping the incoming fixes by boat class.
 * 
 * @author D054528 (Frederik Petersen)
 *
 */
public interface BasePolarClusterKey {

    @Dimension(messageKey = "boatClass")
    BoatClass getBoatClass();

}
