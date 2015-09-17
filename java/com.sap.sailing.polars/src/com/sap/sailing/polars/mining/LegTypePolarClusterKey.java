package com.sap.sailing.polars.mining;

import com.sap.sailing.domain.common.LegType;
import com.sap.sse.datamining.annotations.Dimension;

public interface LegTypePolarClusterKey extends BasePolarClusterKey {

    @Dimension(messageKey = "legType")
    LegType getLegType();

}
