package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.tracking.GPSFix;

public class DynamicGPSFixTrackImpl<ItemType> extends DynamicTrackImpl<ItemType, GPSFix> {
    private static final long serialVersionUID = 4035953954507697564L;

    public DynamicGPSFixTrackImpl(ItemType trackedItem, long millisecondsOverWhichToAverage) {
        super(trackedItem, millisecondsOverWhichToAverage);
    }

    @Override
    public void addGPSFix(GPSFix gpsFix) {
        super.addGPSFix(new CompactGPSFixImpl(gpsFix));
    }
}
