package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.tracking.BravoFixTrack;
import com.sap.sse.datamining.annotations.Connector;

public interface HasBravoTrackContext {
    @Connector(scanForStatistics=false)
    HasRaceOfCompetitorContext getRaceOfCompetitorContext();
    
    BravoFixTrack<Competitor> getBravoFixTrack();
}
