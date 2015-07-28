package com.sap.sse.datamining.test.domain;

import com.sap.sse.datamining.shared.annotations.Statistic;
import com.sap.sse.datamining.shared.data.Unit;

public interface Test_Leg {
    
    @Statistic(messageKey="DistanceTraveled", resultUnit=Unit.Meters, resultDecimals=0)
    public double getDistanceTraveled();

}
