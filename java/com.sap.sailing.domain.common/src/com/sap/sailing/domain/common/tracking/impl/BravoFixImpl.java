package com.sap.sailing.domain.common.tracking.impl;

import com.sap.sailing.domain.common.tracking.BravoFix;
import com.sap.sailing.domain.common.tracking.DoubleVectorFix;
import com.sap.sse.common.TimePoint;

public class BravoFixImpl implements BravoFix {
    private static final long serialVersionUID = 2033254212013221160L;
    
    private final DoubleVectorFix fix;

    public BravoFixImpl(DoubleVectorFix fix) {
        this.fix = fix;
    }

    @Override
    public double get(String valueName) {
        // TODO Auto-generated method stub
//        int index = BravoSensorDataMetadata.INSTANCE.
        return 0;
    }

    @Override
    public TimePoint getTimePoint() {
        return fix.getTimePoint();
    }

    @Override
    public double getRideHeight() {
        // TODO Auto-generated method stub
//        fix.get(index);
        return 0;
    }

}
