package com.sap.sailing.datamining.test.function.test_classes;

import com.sap.sailing.datamining.annotations.SideEffectFreeValue;

public interface ExtendingInterface {

    @SideEffectFreeValue("raceNameLength")
    public int getRaceNameLength();
    
}
