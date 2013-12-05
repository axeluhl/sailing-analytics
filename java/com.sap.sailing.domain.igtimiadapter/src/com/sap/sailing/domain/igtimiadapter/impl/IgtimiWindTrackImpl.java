package com.sap.sailing.domain.igtimiadapter.impl;

import com.sap.sailing.domain.tracking.impl.WindTrackImpl;

public class IgtimiWindTrackImpl extends WindTrackImpl {
    private static final long serialVersionUID = 2362868507967460430L;

    public IgtimiWindTrackImpl(long millisecondsOverWhichToAverage, String deviceSerialNumber) {
        super(millisecondsOverWhichToAverage, /* useSpeed */ true, /* nameForReadWriteLock */ "Igtimi wind track for device "+deviceSerialNumber);
    }

    public IgtimiWindTrackImpl(long millisecondsOverWhichToAverage, double baseConfidence, String deviceSerialNumber) {
        super(millisecondsOverWhichToAverage, baseConfidence, /* useSpeed */ true, /* nameForReadWriteLock */ "Igtimi wind track for device "+deviceSerialNumber);
    }

    
}
