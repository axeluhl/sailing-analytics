package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.tracking.BravoFix;
import com.sap.sse.common.TimePoint;
import com.sap.sse.datamining.annotations.Connector;
import com.sap.sse.datamining.annotations.Statistic;

public interface HasBravoFixContext extends HasWindOnTrackedLeg {
    @Connector(scanForStatistics=false)
    HasTrackedLegOfCompetitorContext getTrackedLegOfCompetitorContext();
    
    @Connector(ordinal=1)
    BravoFix getBravoFix();
    
    @Connector(messageKey="Speed")
    SpeedWithBearing getSpeed();
    
    @Connector(messageKey="Wind")
    default Wind getWind() {
        return HasWindOnTrackedLeg.super.getWind();
    }
    
    @Statistic(messageKey="AbsoluteTrueWindAngle")
    Bearing getAbsoluteTrueWindAngle() throws NoWindException;

    @Override
    default TimePoint getTimePoint() {
        return getBravoFix().getTimePoint();
    }
    
    @Connector(messageKey="VMG")
    Speed getVelocityMadeGood();
}