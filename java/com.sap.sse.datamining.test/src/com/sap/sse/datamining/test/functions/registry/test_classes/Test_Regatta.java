package com.sap.sse.datamining.test.functions.registry.test_classes;

import java.util.Collection;

public interface Test_Regatta extends Test_Named {
    
    public Test_BoatClass getBoatClass();
    
    public Collection<Test_Race> getRaces();

}
