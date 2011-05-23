package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Bearing;
import com.sap.sailing.domain.base.SpeedWithBearing;

public abstract class AbstractSpeedWithBearingImpl extends AbstractSpeedImpl implements SpeedWithBearing {
    private final Bearing bearing;
    
    protected AbstractSpeedWithBearingImpl(Bearing bearing) {
        this.bearing = bearing;
    }

    @Override
    public Bearing getBearing() {
        return bearing;
    }

}
