package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.common.AbstractDistance;
import com.sap.sailing.domain.common.Distance;

public class MeterDistance extends AbstractDistance {
    private final double meters;
    
    public MeterDistance(double meters) {
        this.meters = meters;
    }
    
    @Override
    public double getMeters() {
        return meters;
    }

    @Override
    public Distance scale(double factor) {
        return new MeterDistance(factor * meters);
    }

}
