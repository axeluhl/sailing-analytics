package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Tack;
import com.sap.sse.datamining.annotations.Connector;
import com.sap.sse.datamining.annotations.Dimension;
import com.sap.sse.datamining.annotations.Statistic;

public interface HasMarkPassingContext extends HasWind, HasManeuver, HasTrackedLegOfCompetitor {
    @Connector(scanForStatistics=false)
    HasTrackedLegOfCompetitorContext getTrackedLegOfCompetitorContext();

    //TODO Clean-Up:
    // Move Dimensions and Statistics to Maneuver/MarkPassingManeuver and connect to it
    // Find a way to implement HasManeuverContext and HasMarkPassingManeuver with a base class
    @Dimension(messageKey="Tack", ordinal=12)
    Tack getTack();
    
    @Connector(messageKey="Waypoint", ordinal=13)
    Waypoint getWaypoint();
    
    @Dimension(messageKey="PassingSide", ordinal=13)
    NauticalSide getPassingSide();

    @Connector(messageKey="SpeedBefore", ordinal=0)
    SpeedWithBearing getSpeedBefore();
    @Connector(messageKey="SpeedAfter", ordinal=1)
    SpeedWithBearing getSpeedAfter();
    
    @Statistic(messageKey="DirectionChange", resultDecimals=2, ordinal=2)
    Double getDirectionChangeInDegrees();
    
    @Statistic(messageKey="RelativeScore", ordinal=3, resultDecimals=2)
    Double getRelativeRank();
    
    @Statistic(messageKey="AbsoluteRank", ordinal=4, resultDecimals=2)
    Double getAbsoluteRank();
    
}