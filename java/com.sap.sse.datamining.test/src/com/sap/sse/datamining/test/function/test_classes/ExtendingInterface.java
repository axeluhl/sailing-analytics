package com.sap.sse.datamining.test.function.test_classes;

import com.sap.sse.datamining.annotations.SideEffectFreeValue;

public interface ExtendingInterface {

    @SideEffectFreeValue("raceNameLength")
    public int getRaceNameLength();
    
}
