package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.tracking.BravoFix;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Speed;
import com.sap.sse.datamining.annotations.Connector;
import com.sap.sse.datamining.annotations.Statistic;

public interface HasBravoFixContext {
    @Connector(scanForStatistics=false)
    HasTrackedLegOfCompetitorContext getTrackedLegOfCompetitorContext();
    
    @Connector(ordinal=1)
    BravoFix getBravoFix();
    
    @Connector(messageKey="")
    SpeedWithBearing getSpeed();
    
    @Connector(messageKey="Wind")
    default Wind getWind() {
        return getTrackedLegOfCompetitorContext().getWind();
    }
    
    @Statistic(messageKey="TrueWindAngle")
    Bearing getTrueWindAngle() throws NoWindException;

    @Statistic(messageKey="AbsoluteTrueWindAngle")
    Bearing getAbsoluteTrueWindAngle() throws NoWindException;

    @Connector(messageKey="VMG")
    Speed getVelocityMadeGood();
}