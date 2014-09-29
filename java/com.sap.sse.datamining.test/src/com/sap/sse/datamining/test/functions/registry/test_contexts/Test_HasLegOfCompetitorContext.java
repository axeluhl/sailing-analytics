package com.sap.sse.datamining.test.functions.registry.test_contexts;

import com.sap.sse.datamining.shared.annotations.Connector;
import com.sap.sse.datamining.shared.annotations.Dimension;
import com.sap.sse.datamining.test.functions.registry.test_classes.Test_Competitor;
import com.sap.sse.datamining.test.functions.registry.test_classes.Test_Leg;

public interface Test_HasLegOfCompetitorContext extends Test_HasRaceContext {
    
    @Connector
    public Test_Leg getLeg();

    @Dimension(messageKey="LegNumber")
    public int getLegNumber();

    @Connector
    public Test_Competitor getCompetitor();
    
}
