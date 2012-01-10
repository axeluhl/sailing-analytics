package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Mile;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Speed;

public abstract class AbstractDistance implements Distance {

    @Override
    public boolean equals(Object o) {
        return (o instanceof Distance) && this.compareTo((Distance) o) == 0;
    }
    
    @Override
    public int hashCode() {
        return 9128347 ^ (int) getMeters();
    }
    
    @Override
    public int compareTo(Distance o) {
        return getMeters() > o.getMeters() ? 1 : getMeters() == o.getMeters() ? 0 : -1;
    }

    @Override
    public Speed inTime(long milliseconds) {
        return new KilometersPerHourSpeedImpl(getKilometers() * 1000. * 3600. / milliseconds);
    }

    @Override
    public double getNauticalMiles() {
        return getMeters() / Mile.METERS_PER_NAUTICAL_MILE;
    }

    @Override
    public double getMeters() {
        return getNauticalMiles() * Mile.METERS_PER_NAUTICAL_MILE;
    }

    @Override
    public double getKilometers() {
        return getMeters() / 1000;
    }

    @Override
    public double getGeographicalMiles() {
        return getMeters() / Mile.METERS_PER_GEOGRAPHICAL_MILE;
    }

    @Override
    public double getSeaMiles() {
        return getMeters() / Mile.METERS_PER_SEA_MILE;
    }

    @Override
    public double getCentralAngleDeg() {
        return getCentralAngleRad() / Math.PI * 180.; // one geographical mile equals one minute
    }

    @Override
    public double getCentralAngleRad() {
        return getCentralAngleDeg() * Math.PI / 180.;
    }
    
    @Override
    public String toString() {
        return getMeters()+"m";
    }
}
