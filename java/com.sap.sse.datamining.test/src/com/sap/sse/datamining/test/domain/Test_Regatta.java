package com.sap.sse.datamining.test.domain;

import java.util.Collection;

public interface Test_Regatta extends Test_Named {
    
    public Test_BoatClass getBoatClass();
    
    public int getYear();
    
    public Collection<Test_Race> getRaces();

}
