package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.datamining.annotations.Connector;
import com.sap.sse.datamining.annotations.Dimension;

public interface HasCompetitorDayContext {
    @Connector(messageKey="Race")
    HasRaceOfCompetitorContext getRaceOfCompetitor();
    
    @Connector
    Competitor getCompetitor();
    
    @Dimension(messageKey="Day")
    String getDayAsISO();
}
