package com.sap.sse.datamining.test.function.test_classes;

import com.sap.sse.datamining.annotations.SideEffectFreeValue;

public interface DataTypeInterface {
    
    @SideEffectFreeValue("speedInKnots")
    public int getSpeedInKnots();

}
