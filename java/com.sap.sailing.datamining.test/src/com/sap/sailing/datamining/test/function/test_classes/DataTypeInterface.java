package com.sap.sailing.datamining.test.function.test_classes;

import com.sap.sailing.datamining.annotations.SideEffectFreeValue;

public interface DataTypeInterface {
    
    @SideEffectFreeValue("speedInKnots")
    public int getSpeedInKnots();

}
