package com.sap.sse.datamining.test.data.impl;

import com.sap.sse.datamining.annotations.Statistic;

public interface DataTypeInterface {
    
    @Statistic(messageKey="speedInKnots", resultDecimals=2)
    public int getSpeedInKnots();

}
