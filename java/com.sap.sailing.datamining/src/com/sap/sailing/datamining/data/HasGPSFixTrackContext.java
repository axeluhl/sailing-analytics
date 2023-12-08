package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.datamining.annotations.Connector;
import com.sap.sse.datamining.annotations.Statistic;

public interface HasGPSFixTrackContext {
    @Connector(scanForStatistics=false)
    HasRaceOfCompetitorContext getRaceOfCompetitorContext();
    
    GPSFixTrack<Competitor, GPSFixMoving> getGPSFixTrack();
    
    @Statistic(messageKey="timeSpentOnTackType", resultDecimals=1)
    Duration getTimeSpentTackType();

    @Statistic(messageKey="TackTypeDistance", resultDecimals=1)
    Distance getDistanceSpentTackType();
}
