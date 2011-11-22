package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.tracking.GPSFix;

public abstract class AbstractGPSFixImpl implements GPSFix {
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
}
