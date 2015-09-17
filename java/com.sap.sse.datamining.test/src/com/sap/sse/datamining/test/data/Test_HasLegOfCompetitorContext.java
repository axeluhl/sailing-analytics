package com.sap.sse.datamining.test.data;

import com.sap.sse.datamining.annotations.Connector;
import com.sap.sse.datamining.annotations.Dimension;
import com.sap.sse.datamining.test.domain.Test_Competitor;
import com.sap.sse.datamining.test.domain.Test_Leg;

public interface Test_HasLegOfCompetitorContext {
    
    @Connector(scanForStatistics=false)
    public Test_HasRaceContext getRaceContext();
    
    @Connector
    public Test_Leg getLeg();

    @Dimension(messageKey="LegNumber")
    public int getLegNumber();

    @Connector
    public Test_Competitor getCompetitor();
    
}
