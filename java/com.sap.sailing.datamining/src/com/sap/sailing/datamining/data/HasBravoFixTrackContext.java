package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.tracking.BravoFixTrack;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.datamining.annotations.Connector;
import com.sap.sse.datamining.annotations.Statistic;

public interface HasBravoFixTrackContext {
    @Connector(scanForStatistics=false)
    HasRaceOfCompetitorContext getRaceOfCompetitorContext();
    
    BravoFixTrack<Competitor> getBravoFixTrack();
    
    @Statistic(messageKey="timeSpentFoiling", resultDecimals=1)
    Duration getTimeSpentFoiling();

    @Statistic(messageKey="FoilingDistance", resultDecimals=1)
    Distance getDistanceSpentFoiling();
}
