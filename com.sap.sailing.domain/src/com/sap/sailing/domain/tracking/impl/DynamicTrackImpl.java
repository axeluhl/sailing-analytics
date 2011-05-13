package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.tracking.DynamicTrack;
import com.sap.sailing.domain.tracking.GPSFix;

public class DynamicTrackImpl<ItemType, FixType extends GPSFix> extends
        TrackImpl<ItemType, FixType> implements DynamicTrack<ItemType, FixType> {

    public DynamicTrackImpl(ItemType trackedItem) {
        super(trackedItem);
    }

    @Override
    public void addGPSFix(FixType gpsFix) {
        fixes.add(gpsFix);
    }

}
