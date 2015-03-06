package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sse.datamining.shared.annotations.Connector;
import com.sap.sse.datamining.shared.annotations.Dimension;

public interface HasMarkPassingContext {
    
    @Connector
    HasTrackedRaceContext getTrackedRaceContext();

    @Connector
    MarkPassing getMarkPassing();
    
    @Connector(messageKey="Competitor")
    Competitor getCompetitor();
    
    @Connector(messageKey="Speed", ordinal=1)
    Speed getSpeed();
    
    @Connector(messageKey="SpeedTenSecondsBefore", ordinal=2)
    Speed getSpeedTenSecondsBefore();
    
    @Dimension(messageKey="LegType", ordinal=6)
    String getPreviousLegTypeSignifier() throws NoWindException;
}