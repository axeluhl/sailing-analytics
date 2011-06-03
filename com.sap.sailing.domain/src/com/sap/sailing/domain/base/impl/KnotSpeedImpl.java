package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Speed;

public class KnotSpeedImpl extends AbstractSpeedImpl implements Speed {
    private final double knots;
    
    public KnotSpeedImpl(double knots) {
        this.knots = knots;
    }
    
    @Override
    public double getKnots() {
        return knots;
    }
}
