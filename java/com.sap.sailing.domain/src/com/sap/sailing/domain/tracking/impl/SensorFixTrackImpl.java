package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.common.tracking.SensorFix;
import com.sap.sailing.domain.tracking.DynamicSensorFixTrack;

public class SensorFixTrackImpl<FixT extends SensorFix> extends DynamicTrackImpl<FixT> implements
        DynamicSensorFixTrack<FixT> {

    private static final long serialVersionUID = 2359773532545067069L;
    
    private final Iterable<String> valueNames;

    public SensorFixTrackImpl(Iterable<String> valueNames, String nameForReadWriteLock) {
        super(nameForReadWriteLock);
        this.valueNames = valueNames;
    }

    @Override
    public Iterable<String> getValueNames() {
        return valueNames;
    }

}