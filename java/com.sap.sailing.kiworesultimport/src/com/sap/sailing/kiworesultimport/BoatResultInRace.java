package com.sap.sailing.kiworesultimport;

import com.sap.sailing.domain.common.MaxPointsReason;

public interface BoatResultInRace {
    String getStatus();
    
    Double getPoints();

    Integer getRaceNumber();

    MaxPointsReason getMaxPointsReason();

    boolean isDiscarded();
}
