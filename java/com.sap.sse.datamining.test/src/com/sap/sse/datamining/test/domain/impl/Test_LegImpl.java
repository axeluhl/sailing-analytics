package com.sap.sse.datamining.test.domain.impl;

import com.sap.sse.datamining.test.domain.Test_Leg;

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
