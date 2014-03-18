package com.sap.sse.datamining.test.function.test_classes;

import com.sap.sse.datamining.shared.annotations.SideEffectFreeValue;

public interface ExtendingInterface {

    @SideEffectFreeValue(messageKey="raceNameLength")
    public int getRaceNameLength();
    
}
