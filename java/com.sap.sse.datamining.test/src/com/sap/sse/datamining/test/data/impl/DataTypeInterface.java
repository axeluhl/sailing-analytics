package com.sap.sse.datamining.test.data.impl;

import com.sap.sse.datamining.annotations.Statistic;
import com.sap.sse.datamining.annotations.data.Unit;

public interface DataTypeInterface {
    
    @Statistic(messageKey="speedInKnots", resultUnit=Unit.Knots, resultDecimals=2)
    public int getSpeedInKnots();

}
