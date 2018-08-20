package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sse.common.Bearing;
import com.sap.sse.datamining.annotations.Connector;
import com.sap.sse.datamining.annotations.Statistic;

public interface HasGPSFixContext {
    @Connector(scanForStatistics=false)
    public HasTrackedLegOfCompetitorContext getTrackedLegOfCompetitorContext();
    
    @Connector(ordinal=1)
    public GPSFixMoving getGPSFix();

    @Statistic(messageKey="TrueWindAngle")
    Bearing getTrueWindAngle() throws NoWindException;
    
    @Statistic(messageKey="AbsoluteTrueWindAngle")
    Bearing getAbsoluteTrueWindAngle() throws NoWindException;
}