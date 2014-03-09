package com.sap.sse.datamining.test.function.test_classes;

import com.sap.sse.datamining.annotations.SideEffectFreeValue;
import com.sap.sse.datamining.shared.Unit;

public interface DataTypeInterface {
    
    @SideEffectFreeValue(messageKey="speedInKnots", resultUnit=Unit.Knots, resultDecimals=2)
    public int getSpeedInKnots();

}
