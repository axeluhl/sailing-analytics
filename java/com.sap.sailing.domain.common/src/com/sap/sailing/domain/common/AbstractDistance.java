package com.sap.sailing.domain.common;

import com.sap.sailing.domain.common.impl.KilometersPerHourSpeedImpl;
import com.sap.sailing.domain.common.impl.NauticalMileDistance;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Speed;

public abstract class AbstractDistance implements Distance {

    private static final long serialVersionUID = 4393500221539639333L;

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
        final double meters = getMeters();
        final double otherMeters = o.getMeters();
        return meters > otherMeters ? 1 : meters == otherMeters ? 0 : -1;
    }

    @Override
    public Speed inTime(long milliseconds) {
        return new KilometersPerHourSpeedImpl(getKilometers() * 1000. * 3600. / milliseconds);
    }
    
    @Override
    public Speed inTime(Duration duration) {
        return inTime(duration.asMillis());
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
    
    @Override
    public Distance add(Distance d) {
        return new NauticalMileDistance(getNauticalMiles()+d.getNauticalMiles());
    }

    @Override
    public double divide(Distance other) {
        return getMeters() / other.getMeters();
    }

    @Override
    public Distance abs() {
        final Distance result;
        if (getMeters() >= 0) {
            result = this;
        } else {
            result = scale(-1);
        }
        return result;
    }
}
