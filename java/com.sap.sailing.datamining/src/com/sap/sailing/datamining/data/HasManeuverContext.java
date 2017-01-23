package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sse.datamining.annotations.Connector;
import com.sap.sse.datamining.annotations.Dimension;
import com.sap.sse.datamining.annotations.Statistic;

public interface HasManeuverContext extends HasWind {
    
    @Connector(scanForStatistics=false)
    HasTrackedLegOfCompetitorContext getTrackedLegOfCompetitorContext();
    
    Maneuver getManeuver();
    
    //TODO Clean-Up:
    // Move Dimensions and Statistics to Maneuver and connect to it
    @Dimension(messageKey="ManeuverType", ordinal=12)
    ManeuverType getManeuverType();
    
    @Dimension(messageKey="Tack", ordinal=13)
    Tack getTack();

    @Connector(messageKey="SpeedBefore", ordinal=0)
    SpeedWithBearing getSpeedBefore();
    @Connector(messageKey="SpeedAfter", ordinal=1)
    SpeedWithBearing getSpeedAfter();
    
    @Statistic(messageKey="DirectionChange", resultDecimals=2, ordinal=2)
    Double getDirectionChangeInDegrees();

    @Statistic(messageKey="AbsoluteDirectionChange", resultDecimals=2, ordinal=2)
    Double getAbsoluteDirectionChangeInDegrees();
    
    @Dimension(messageKey="ToSide")
    NauticalSide getToSide();
    
    @Statistic(messageKey="ManeuverLoss", resultDecimals=0, ordinal=3)
    Distance getManeuverLoss();

}
