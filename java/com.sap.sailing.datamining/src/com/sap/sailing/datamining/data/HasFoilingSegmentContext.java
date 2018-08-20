package com.sap.sailing.datamining.data;

import java.util.Locale;

import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Wind;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.datamining.annotations.Connector;
import com.sap.sse.datamining.annotations.Dimension;
import com.sap.sse.datamining.annotations.Statistic;
import com.sap.sse.datamining.shared.impl.dto.ClusterDTO;
import com.sap.sse.i18n.ResourceBundleStringMessages;

public interface HasFoilingSegmentContext {
    @Connector(scanForStatistics=false)
    HasBravoFixTrackContext getBravoFixTrackContext();
    
    TimePoint getStartOfFoilingSegment();
    
    TimePoint getEndOfFoilingSegment();
    
    @Dimension(messageKey="FoilingSegmentName")
    String getName();
    
    @Statistic(messageKey="FoilingDuration")
    Duration getDuration();
    
    @Statistic(messageKey="FoilingDistance")
    Distance getDistance();
    
    @Statistic(messageKey="TakeoffSpeedInKnotsInKnots", resultDecimals=1)
    Double getTakeoffSpeedInKnots();

    @Statistic(messageKey="LandingSpeedInKnots", resultDecimals=1)
    Double getLandingSpeedInKnots();
    
    @Statistic(messageKey="AbsoluteTrueWindAngleAtTakeoff", resultDecimals=1)
    Bearing getAbsoluteTrueWindAngleAtTakeoffInDegrees() throws NoWindException;
    
    @Statistic(messageKey="AbsoluteTrueWindAngleAtLanding", resultDecimals=1)
    Bearing getAbsoluteTrueWindAngleAtLandingInDegrees() throws NoWindException;

    @Statistic(messageKey="AverageAbsoluteTrueWindAngle", resultDecimals=1)
    Bearing getAverageAbsoluteTrueWindAngle() throws NoWindException;
    
    @Dimension(messageKey="WindStrengthInBeaufortAtTakeoff")
    ClusterDTO getWindStrengthAsBeaufortClusterAtTakeoff(Locale locale, ResourceBundleStringMessages stringMessages);

    @Dimension(messageKey="WindStrengthInBeaufortAtLanding")
    ClusterDTO getWindStrengthAsBeaufortClusterAtLanding(Locale locale, ResourceBundleStringMessages stringMessages);

    @Connector(messageKey="WindAtTakeoff")
    Wind getWindAtTakeoff();

    @Connector(messageKey="WindAtLanding")
    Wind getWindAtLanding();
    
    @Dimension(messageKey="StartsOnLegType")
    LegType getStartsOnLegType() throws NoWindException;

    @Dimension(messageKey="EndsOnLegType")
    LegType getEndsOnLegType() throws NoWindException;
}
