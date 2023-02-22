package com.sap.sse.security.datamining.data;

import com.sap.sse.common.Duration;
import com.sap.sse.datamining.annotations.Connector;
import com.sap.sse.datamining.annotations.Dimension;
import com.sap.sse.datamining.annotations.Statistic;

public interface HasSessionContext {
    @Connector(messageKey = "User", scanForStatistics = false)
    HasUserContext getUserContext();
    
    @Dimension(messageKey = "Valid")
    default boolean isValid() {
        return getDurationUntilSessionExpiry().compareTo(Duration.NULL) > 0;
    }
    
    @Dimension(messageKey = "StartYear")
    int getStartYear();

    @Dimension(messageKey = "StartMonth")
    String getStartMonth();
    
    @Statistic(messageKey="DurationSinceLastAccess")
    Duration getDurationSinceLastAccess();
    
    @Statistic(messageKey="DurationUntilSessionExpiry")
    Duration getDurationUntilSessionExpiry();
}
