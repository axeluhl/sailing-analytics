package com.sap.sailing.datamining.data;

import java.util.Locale;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Wind;
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
    
    @Statistic(messageKey="FoilingDuration")
    Duration getDuration();
    
    @Statistic(messageKey="FoilingDistance")
    Distance getDistance();
    
    @Statistic(messageKey="TakeoffSpeedInKnotsInKnots")
    Double getTakeoffSpeedInKnots();

    @Statistic(messageKey="LandingSpeedInKnots")
    Double getLandingSpeedInKnots();
    
    @Statistic(messageKey="TrueWindAngleAtTakeoffInDegrees")
    Bearing getTrueWindAngleAtTakeoffInDegrees() throws NoWindException;
    
    @Statistic(messageKey="TrueWindAngleAtLandingInDegrees")
    Bearing getTrueWindAngleAtLandingInDegrees() throws NoWindException;
    
    @Dimension(messageKey="WindStrengthInBeaufortAtTakeoff")
    ClusterDTO getWindStrengthAsBeaufortClusterAtTakeoff(Locale locale, ResourceBundleStringMessages stringMessages);

    @Dimension(messageKey="WindStrengthInBeaufortAtLanding")
    ClusterDTO getWindStrengthAsBeaufortClusterAtLanding(Locale locale, ResourceBundleStringMessages stringMessages);

    @Connector(messageKey="WindAtTakeoff")
    Wind getWindAtTakeoff();

    @Connector(messageKey="WindAtLanding")
    Wind getWindAtLanding();
}
