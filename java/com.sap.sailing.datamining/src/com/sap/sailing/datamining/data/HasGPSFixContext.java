package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.TackType;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;
import com.sap.sse.datamining.annotations.Connector;
import com.sap.sse.datamining.annotations.Dimension;
import com.sap.sse.datamining.annotations.Statistic;

public interface HasGPSFixContext {
    @Connector(scanForStatistics = false)
    public HasTrackedLegOfCompetitorContext getTrackedLegOfCompetitorContext();

    @Connector(ordinal = 1)
    public GPSFixMoving getGPSFix();
    
    @Dimension(messageKey="TackType", ordinal=6)
    TackType getTackType();

    @Statistic(messageKey = "TrueWindAngle")
    Bearing getTrueWindAngle() throws NoWindException;

    @Statistic(messageKey = "AbsoluteTrueWindAngle")
    Bearing getAbsoluteTrueWindAngle() throws NoWindException;

    @Statistic(messageKey = "VMG", resultDecimals = 2)
    SpeedWithBearing getVelocityMadeGood();

    @Statistic(messageKey = "XTE", resultDecimals = 2)
    Distance getXTE();

    @Statistic(messageKey = "AbsoluteXTE", resultDecimals = 2)
    Distance getAbsoluteXTE();
}