package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.BuoyMark;

public class BuoyMarkImpl implements BuoyMark {
    private final Buoy buoy;

    public BuoyMarkImpl(String buoyName) {
        buoy = new BuoyImpl(buoyName);
    }

    @Override
    public Buoy getBuoy() {
        return buoy;
    }

    @Override
    public String getName() {
        return getBuoy().getName();
    }

    @Override
    public String toString() {
        return getName();
    }
}
