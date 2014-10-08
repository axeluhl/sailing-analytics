package com.sap.sse.datamining.test.functions.registry.test_classes;

import com.sap.sse.datamining.shared.annotations.Connector;

public interface Test_Competitor {
    
    @Connector
    public Test_Team getTeam();
    
    @Connector
    public Test_Boat getBoat();

}
