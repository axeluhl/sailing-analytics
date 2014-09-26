package com.sap.sse.datamining.test.functions.registry.test_contexts;

import com.sap.sse.datamining.shared.annotations.Connector;
import com.sap.sse.datamining.shared.annotations.Dimension;
import com.sap.sse.datamining.test.functions.registry.test_classes.Test_BoatClass;
import com.sap.sse.datamining.test.functions.registry.test_classes.Test_Race;
import com.sap.sse.datamining.test.functions.registry.test_classes.Test_Regatta;

public interface Test_HasRaceContext {
    
    @Connector
    public Test_Regatta getRegatta();

    @Connector
    public Test_Race getRace();

    @Connector
    public Test_BoatClass getBoatClass();
    
    @Dimension(messageKey="Year")
    public int getYear();

}
