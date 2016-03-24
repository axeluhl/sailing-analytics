package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.impl.CompactGPSFixImpl;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;

public class DynamicGPSFixTrackImpl<ItemType> extends GPSFixTrackImpl<ItemType, GPSFix> implements DynamicGPSFixTrack<ItemType, GPSFix> {
    private static final long serialVersionUID = 4035953954507697564L;

    public DynamicGPSFixTrackImpl(ItemType trackedItem, long millisecondsOverWhichToAverage) {
        super(trackedItem, millisecondsOverWhichToAverage);
    }

    public void addGPSFix(GPSFix gpsFix) {
        add(gpsFix, /* replace */ true);
    }
    
    @Override
    public boolean add(GPSFix fix) {
        return super.add(fix); // ends up calling this.add(fix, false) where conversion in CompactGPSFixImpl will happen
    }

    @Override
    public boolean add(GPSFix fix, boolean replace) {
        return super.add(new CompactGPSFixImpl(fix), replace);
    }

    @Override
    public void setMillisecondsOverWhichToAverage(long millisecondsOverWhichToAverage) {
        super.setMillisecondsOverWhichToAverage(millisecondsOverWhichToAverage);
    }
}
