package com.sap.sse.datamining.test.functions.registry.test_classes.impl;

import com.sap.sse.datamining.test.functions.registry.test_classes.Test_Leg;

public class Test_LegImpl implements Test_Leg {

    private double distanceTraveled;

    public Test_LegImpl(double distanceTraveled) {
        this.distanceTraveled = distanceTraveled;
    }

    @Override
    public double getDistanceTraveled() {
        return distanceTraveled;
    }

}
