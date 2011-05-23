package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Bearing;

public abstract class AbstractBearing implements Bearing {
    @Override
    public String toString() {
        return ""+getDegrees()+"°";
    }
}
