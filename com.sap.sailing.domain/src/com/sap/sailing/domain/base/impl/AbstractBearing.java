package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Bearing;

public abstract class AbstractBearing implements Bearing {
    @Override
    public Bearing reverse() {
        if (getDegrees() >= 180) {
            return new DegreeBearingImpl(getDegrees()-180);
        } else {
            return new DegreeBearingImpl(getDegrees()+180);
        }
    }

    @Override
    public String toString() {
        return ""+getDegrees()+"°";
    }
}
