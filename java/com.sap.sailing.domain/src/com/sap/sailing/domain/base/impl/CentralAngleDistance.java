package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Mile;
import com.sap.sailing.domain.common.Distance;


public class CentralAngleDistance extends AbstractDistance {
    private final double centralAngleRad;
    
    public CentralAngleDistance(double centralAngleRad) {
        super();
        this.centralAngleRad = centralAngleRad;
    }

    @Override
    public double getMeters() {
        return getGeographicalMiles() * Mile.METERS_PER_GEOGRAPHICAL_MILE;
    }

    @Override
    public double getCentralAngleRad() {
        return centralAngleRad;
    }

    @Override
    public double getGeographicalMiles() {
        return getCentralAngleDeg() * 60.;
    }

    @Override
    public Distance scale(double factor) {
        return new CentralAngleDistance(factor*getCentralAngleRad());
    }
}
