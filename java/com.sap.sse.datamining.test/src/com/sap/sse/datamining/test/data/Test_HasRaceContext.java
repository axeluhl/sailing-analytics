package com.sap.sse.datamining.test.data;

import com.sap.sse.datamining.annotations.Connector;
import com.sap.sse.datamining.annotations.Dimension;
import com.sap.sse.datamining.test.domain.Test_BoatClass;
import com.sap.sse.datamining.test.domain.Test_Race;
import com.sap.sse.datamining.test.domain.Test_Regatta;

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
