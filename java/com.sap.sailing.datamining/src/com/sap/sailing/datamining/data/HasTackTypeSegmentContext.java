package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.TackType;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.datamining.annotations.Connector;
import com.sap.sse.datamining.annotations.Dimension;
import com.sap.sse.datamining.annotations.Statistic;

public interface HasTackTypeSegmentContext {
    @Connector(scanForStatistics=false)
    HasGPSFixTrackContext getGPSFixTrackContext();
    
    TimePoint getStartOfTackTypeSegment();
    
    TimePoint getEndOfTackTypeSegment();
    
    @Dimension(messageKey="TackType")
    TackType getTackType();
    
    @Dimension(messageKey="LegType")
    LegType getLegType() throws NoWindException;
    
    @Dimension(messageKey="TackTypeSegmentName")
    String getName();
    
    @Statistic(messageKey="TackTypeDuration")
    Duration getDuration();
    
    @Statistic(messageKey="TackTypeDistance")
    Distance getDistance();
}
