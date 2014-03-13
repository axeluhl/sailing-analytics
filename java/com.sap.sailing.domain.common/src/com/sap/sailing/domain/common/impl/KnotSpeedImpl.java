package com.sap.sailing.domain.common.impl;

import com.sap.sailing.domain.common.AbstractSpeedImpl;
import com.sap.sailing.domain.common.Speed;

public class KnotSpeedImpl extends AbstractSpeedImpl implements Speed {
    private static final long serialVersionUID = 5150851454271610069L;
    private final double knots;
    
    public KnotSpeedImpl(double knots) {
        this.knots = knots;
    }
    
    @Override
    public double getKnots() {
        return knots;
    }
}
