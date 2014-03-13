package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.GPSFix;

public class DynamicGPSFixTrackImpl<ItemType> extends GPSFixTrackImpl<ItemType, GPSFix> implements DynamicGPSFixTrack<ItemType, GPSFix> {
    private static final long serialVersionUID = 4035953954507697564L;

    public DynamicGPSFixTrackImpl(ItemType trackedItem, long millisecondsOverWhichToAverage) {
        super(trackedItem, millisecondsOverWhichToAverage);
    }

    public void addGPSFix(GPSFix gpsFix) {
        add(gpsFix);
    }
    
    @Override
    public boolean add(GPSFix fix) {
        return super.add(new CompactGPSFixImpl(fix));
    }

    @Override
    public void setMillisecondsOverWhichToAverage(long millisecondsOverWhichToAverage) {
        super.setMillisecondsOverWhichToAverage(millisecondsOverWhichToAverage);
    }
}
