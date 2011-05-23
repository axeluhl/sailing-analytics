package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Bearing;
import com.sap.sailing.domain.base.SpeedWithBearing;

public class KilometersPerHourSpeedWithBearingImpl extends KilometersPerHourSpeedImpl implements SpeedWithBearing {
    private final Bearing bearing;
    
    public KilometersPerHourSpeedWithBearingImpl(double speedInKilometersPerHour, Bearing bearing) {
        super(speedInKilometersPerHour);
        this.bearing = bearing;
    }

    @Override
    public Bearing getBearing() {
        return bearing;
    }

}
