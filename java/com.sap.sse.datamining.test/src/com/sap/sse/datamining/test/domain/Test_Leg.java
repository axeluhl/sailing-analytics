package com.sap.sse.datamining.test.domain;

import com.sap.sse.datamining.annotations.Statistic;

public interface Test_Leg {
    
    @Statistic(messageKey="DistanceTraveled", resultDecimals=0)
    public double getDistanceTraveled();

}
