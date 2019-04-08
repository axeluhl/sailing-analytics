package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Wind;
import com.sap.sse.common.TimePoint;
import com.sap.sse.datamining.annotations.Connector;
import com.sap.sse.datamining.annotations.Dimension;
import com.sap.sse.datamining.annotations.Statistic;

public interface HasWindFixContext extends HasWind {
    @Connector(scanForStatistics=false)
    public HasTrackedRaceContext getTrackedRaceContext();
    
    @Statistic(messageKey="WindFix", ordinal=0)
    public Wind getWind();
    
    @Statistic(messageKey="windFrom", resultDecimals=1, ordinal=1)
    double getWindFromDegrees();
    
    @Statistic(messageKey="windSpeedKnots", resultDecimals=1, ordinal=2)
    double getWindSpeedInKnots();

    @Override
    default TimePoint getTimePoint() {
        return getWind().getTimePoint();
    }

    @Override
    default Position getPosition() {
        return getWind().getPosition();
    }

    @Dimension(messageKey="WindSourceType")
    String getWindSourceType();
}
