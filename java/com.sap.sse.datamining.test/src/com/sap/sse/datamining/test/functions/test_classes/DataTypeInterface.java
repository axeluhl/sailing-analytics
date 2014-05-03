package com.sap.sse.datamining.test.functions.test_classes;

import com.sap.sse.datamining.shared.Unit;
import com.sap.sse.datamining.shared.annotations.SideEffectFreeValue;

public interface DataTypeInterface {
    
    @SideEffectFreeValue(messageKey="speedInKnots", resultUnit=Unit.Knots, resultDecimals=2)
    public int getSpeedInKnots();

}
