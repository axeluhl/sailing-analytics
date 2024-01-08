package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sse.datamining.annotations.Connector;

public interface HasGPSFixTrackContext {
    @Connector(scanForStatistics=false)
    HasRaceOfCompetitorContext getRaceOfCompetitorContext();
    
    GPSFixTrack<Competitor, GPSFixMoving> getGPSFixTrack();
}
