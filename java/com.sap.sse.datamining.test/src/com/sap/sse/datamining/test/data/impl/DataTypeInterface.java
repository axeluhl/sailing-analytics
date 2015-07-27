package com.sap.sse.datamining.test.data.impl;

import com.sap.sse.datamining.shared.annotations.Statistic;
import com.sap.sse.datamining.shared.data.Unit;

public interface DataTypeInterface {
    
    @Statistic(messageKey="speedInKnots", resultUnit=Unit.Knots, resultDecimals=2)
    public int getSpeedInKnots();

}
