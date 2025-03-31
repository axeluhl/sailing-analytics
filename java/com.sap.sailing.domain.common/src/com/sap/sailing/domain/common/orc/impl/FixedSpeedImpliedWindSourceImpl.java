package com.sap.sailing.domain.common.orc.impl;

import com.sap.sailing.domain.common.orc.FixedSpeedImpliedWind;
import com.sap.sse.common.Speed;

public class FixedSpeedImpliedWindSourceImpl implements FixedSpeedImpliedWind {
    private static final long serialVersionUID = 1353122921161718934L;
    private final Speed fixedImpliedWindSpeed;
    
    public FixedSpeedImpliedWindSourceImpl(Speed fixedImpliedWindSpeed) {
        super();
        this.fixedImpliedWindSpeed = fixedImpliedWindSpeed;
    }

    @Override
    public Speed getFixedImpliedWindSpeed() {
        return fixedImpliedWindSpeed;
    }

    @Override
    public String toString() {
        return "" + fixedImpliedWindSpeed + " (fixed)";
    }
}
