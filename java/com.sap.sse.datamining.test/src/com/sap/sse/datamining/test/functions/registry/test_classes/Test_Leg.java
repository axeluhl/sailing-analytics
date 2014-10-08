package com.sap.sse.datamining.test.functions.registry.test_classes;

import com.sap.sse.datamining.shared.Unit;
import com.sap.sse.datamining.shared.annotations.Statistic;

public interface Test_Leg {
    
    @Statistic(messageKey="DistanceTraveled", resultUnit=Unit.Meters, resultDecimals=0)
    public double getDistanceTraveled();

}
