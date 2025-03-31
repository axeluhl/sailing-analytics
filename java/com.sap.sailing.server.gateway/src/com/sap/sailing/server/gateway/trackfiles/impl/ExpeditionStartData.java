package com.sap.sailing.server.gateway.trackfiles.impl;

import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sse.common.TimePoint;

public interface ExpeditionStartData {
    Iterable<GPSFix> getStartLinePortFixes();
    
    Iterable<GPSFix> getStartLineStarboardFixes();
    
    Iterable<TimePoint> getStartTimes();
}
