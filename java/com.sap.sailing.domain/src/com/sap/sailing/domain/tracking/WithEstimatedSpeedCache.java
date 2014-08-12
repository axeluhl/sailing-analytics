package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.common.SpeedWithBearing;

public interface WithEstimatedSpeedCache {
    boolean isEstimatedSpeedCached();
    
    SpeedWithBearing getEstimatedSpeed();
    
    void invalidateEstimatedSpeedCache();
    
    void cacheEstimatedSpeed(SpeedWithBearing estimatedSpeed);

}
