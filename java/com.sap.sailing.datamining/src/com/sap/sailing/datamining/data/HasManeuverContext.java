package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sse.datamining.annotations.Connector;
import com.sap.sse.datamining.annotations.Dimension;
import com.sap.sse.datamining.annotations.Statistic;

public interface HasManeuverContext extends HasWindOnTrackedLeg, HasManeuver, HasTrackedLegOfCompetitor {
    @Connector(scanForStatistics=false)
    HasTrackedLegOfCompetitorContext getTrackedLegOfCompetitorContext();

    @Dimension(messageKey="ManeuverType", ordinal=12)
    ManeuverType getManeuverType();
    
    @Dimension(messageKey="ToSide", ordinal=16)
    NauticalSide getToSide();
    
    @Statistic(messageKey="AbsoluteDirectionChange", resultDecimals=2, ordinal=3)
    Double getAbsoluteDirectionChangeInDegrees();

    @Statistic(messageKey="ManeuverLoss", resultDecimals=0, ordinal=4)
    Distance getManeuverLoss();
    
    @Statistic(messageKey="ManeuverEnteringBeatAngle", ordinal=5)
    Double getEnteringBeatAngle();
    
    @Statistic(messageKey="ManeuverExitingBeatAngle", ordinal=6)
    Double getExitingBeatAngle();
    
    @Statistic(messageKey="ManeuverDurationInSeconds", resultDecimals=2)
    Double getManeuverDuration();
    
    @Statistic(messageKey="BeatAngleAtManeuverClimax")
    Double getBeatAngleAtManeuverClimax();
    

}
