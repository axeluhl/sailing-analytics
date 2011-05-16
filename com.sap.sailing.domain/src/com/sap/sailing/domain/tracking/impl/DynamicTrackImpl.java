package com.sap.sailing.domain.tracking.impl;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.tracking.DynamicTrack;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.RawListener;

public class DynamicTrackImpl<ItemType, FixType extends GPSFix> extends
        TrackImpl<ItemType, FixType> implements DynamicTrack<ItemType, FixType> {
    private final Set<RawListener<FixType>> listeners;
    
    public DynamicTrackImpl(ItemType trackedItem) {
        super(trackedItem);
        this.listeners = new HashSet<RawListener<FixType>>();
    }

    @Override
    public void addGPSFix(FixType gpsFix) {
        fixes.add(gpsFix);
    }

    @Override
    public void addListener(RawListener<FixType> listener) {
        listeners.add(listener);
    }

}
