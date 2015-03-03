package com.sap.sailing.polars.mining;

import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.Tack;
import com.sap.sse.datamining.shared.annotations.Dimension;

public interface TackAndLegTypePolarClusterKey extends BasePolarClusterKey {
    
    @Dimension(messageKey = "tack")
    Tack getTack();
    
    @Dimension(messageKey = "legType")
    LegType getLegType();

}
