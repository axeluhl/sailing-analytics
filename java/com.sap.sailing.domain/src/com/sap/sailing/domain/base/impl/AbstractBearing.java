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
    public Bearing add(Bearing diff) {
        double newDeg = getDegrees() + diff.getDegrees();
        if (newDeg > 360) {
            newDeg -= 360;
        } else if (newDeg < 0) {
            newDeg += 360;
        }
        return new DegreeBearingImpl(newDeg);
    }

    @Override
    public String toString() {
        return ""+getDegrees()+"°";
    }
}
