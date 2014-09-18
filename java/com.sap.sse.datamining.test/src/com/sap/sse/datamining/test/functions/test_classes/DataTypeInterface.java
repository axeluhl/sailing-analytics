package com.sap.sse.datamining.test.functions.test_classes;

import com.sap.sse.datamining.shared.Unit;
import com.sap.sse.datamining.shared.annotations.Statistic;

public interface DataTypeInterface {
    
    @Statistic(messageKey="speedInKnots", resultUnit=Unit.Knots, resultDecimals=2)
    public int getSpeedInKnots();

}
