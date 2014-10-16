package com.sap.sse.datamining.test.functions.registry.test_classes;

import java.util.Collection;

public interface Test_Race extends Test_Named {
    
    public Collection<Test_Competitor> getCompetitors();
    
    public Collection<Test_Leg> getLegs();

}
