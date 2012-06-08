package com.sap.sailing.kiworesultimport;

import com.sap.sailing.domain.common.MaxPointsReason;

public interface Race {
    String getStatus();
    
    Double getPoints();

    Integer getNumber();

    MaxPointsReason getMaxPointsReason();
}
