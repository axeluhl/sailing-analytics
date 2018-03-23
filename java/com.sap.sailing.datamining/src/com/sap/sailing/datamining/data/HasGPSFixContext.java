package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sse.common.TimePoint;
import com.sap.sse.datamining.annotations.Connector;
import com.sap.sse.datamining.annotations.Statistic;

public interface HasGPSFixContext extends HasWindOnTrackedLeg {
    @Connector(scanForStatistics=false)
    public HasTrackedLegOfCompetitorContext getTrackedLegOfCompetitorContext();
    
    @Connector(ordinal=1)
    public GPSFixMoving getGPSFix();

    @Override
    default TimePoint getTimePoint() {
        return getGPSFix().getTimePoint();
    }

    @Override
    default Position getPosition() {
        return getGPSFix().getPosition();
    }
    
    @Statistic(messageKey="TrueWindAngle")
    Bearing getTrueWindAngle() throws NoWindException;
    
    @Statistic(messageKey="AbsoluteTrueWindAngle")
    Bearing getAbsoluteTrueWindAngle() throws NoWindException;
}