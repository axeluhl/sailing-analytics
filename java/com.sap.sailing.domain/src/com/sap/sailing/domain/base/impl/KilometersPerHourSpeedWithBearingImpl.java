package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Bearing;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.TimePoint;

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

    @Override
    public Position travelTo(Position pos, TimePoint from, TimePoint to) {
        return pos.translateGreatCircle(getBearing(), this.travel(from, to));
    }

    @Override
    public String toString() {
        return super.toString()+" to "+getBearing().getDegrees()+"°";
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ getBearing().hashCode();
    }
    
    @Override
    public boolean equals(Object object) {
        return super.equals(object) && object instanceof SpeedWithBearing
                && getBearing().equals(((SpeedWithBearing) object).getBearing());
    }
}
