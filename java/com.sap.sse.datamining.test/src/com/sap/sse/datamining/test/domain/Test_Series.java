package com.sap.sse.datamining.test.domain;

import java.util.Collection;

public interface Test_Series extends Test_Named {
    
    public Test_Regatta getRegatta();
    
    public Collection<Test_Race> getRaces();

}
