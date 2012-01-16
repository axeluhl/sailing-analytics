package com.sap.sailing.domain.common.impl;

import com.sap.sailing.domain.common.AbstractPosition;

public class DegreePosition extends AbstractPosition {
    
    private final double lat;
    private final double lng;

    public DegreePosition(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    @Override
    public double getLatDeg() {
        return lat;
    }

    @Override
    public double getLngDeg() {
        return lng;
    }
}
