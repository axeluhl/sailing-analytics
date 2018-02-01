package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sse.datamining.annotations.Connector;
import com.sap.sse.datamining.annotations.Dimension;

public interface HasWindTrackContext {
    @Connector(scanForStatistics=false)
    public HasTrackedRaceContext getTrackedRaceContext();
    
    @Dimension(messageKey="WindSourceType")
    String getWindSourceType();
    
    @Dimension(messageKey="WindSourceName")
    String getWindSourceName();
    
    WindTrack getWindTrack();
}
