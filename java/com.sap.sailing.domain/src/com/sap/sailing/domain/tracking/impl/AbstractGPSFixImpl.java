package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.tracking.GPSFix;

public abstract class AbstractGPSFixImpl implements GPSFix {
    private static final long serialVersionUID = 9037068515469957639L;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getPosition() == null) ? 0 : getPosition().hashCode());
        result = prime * result + ((getTimePoint() == null) ? 0 : getTimePoint().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof GPSFix))
            return false;
        GPSFix other = (GPSFix) obj;
        if (getPosition() == null) {
            if (other.getPosition() != null)
                return false;
        } else if (!getPosition().equals(other.getPosition()))
            return false;
        if (getTimePoint() == null) {
            if (other.getTimePoint() != null)
                return false;
        } else if (!getTimePoint().equals(other.getTimePoint()))
            return false;
        return true;
    }

    @Override
    public SpeedWithBearing getSpeedAndBearingRequiredToReach(GPSFix to) {
        Distance distance = getPosition().getDistance(to.getPosition());
        Bearing bearing = getPosition().getBearingGreatCircle(to.getPosition());
        Speed speed = distance.inTime(to.getTimePoint().asMillis()-getTimePoint().asMillis());
        return new KnotSpeedWithBearingImpl(speed.getKnots(), bearing);
    }
    
    @Override
    public boolean isValidityCached() {
        return false;
    }
    
    @Override
    public boolean isValid() {
        return false;
    }
    
    @Override
    public void invalidateCache() {
    }
    
    @Override
    public void cacheValidity(boolean isValid) {
    }
}
