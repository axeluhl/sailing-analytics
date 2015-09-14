package com.sap.sailing.polars.datamining.data;

import com.sap.sse.datamining.shared.annotations.Dimension;
import com.sap.sse.datamining.shared.impl.dto.ClusterDTO;

public interface HasGPSFixPolarContext {
    
    @Dimension(messageKey="WindRange")
    ClusterDTO getWindSpeedRange();

}
