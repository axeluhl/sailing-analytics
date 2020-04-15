package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.impl.CompactPositionHelper;
import com.sap.sailing.domain.common.tracking.impl.PreciseCompactGPSFixImpl;
import com.sap.sailing.domain.common.tracking.impl.VeryCompactGPSFixImpl;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;

public class DynamicGPSFixTrackImpl<ItemType> extends GPSFixTrackImpl<ItemType, GPSFix> implements DynamicGPSFixTrack<ItemType, GPSFix> {
    private static final long serialVersionUID = 4035953954507697564L;

    /**
     * By default uses lossy compaction. See {@link CompactPositionHelper}.
     */
    public DynamicGPSFixTrackImpl(ItemType trackedItem, long millisecondsOverWhichToAverage) {
        this(trackedItem, millisecondsOverWhichToAverage, /* losslessCompaction */ false);
    }
    
    public DynamicGPSFixTrackImpl(ItemType trackedItem, long millisecondsOverWhichToAverage, boolean losslessCompaction) {
        super(trackedItem, millisecondsOverWhichToAverage, losslessCompaction);
    }

    public boolean addGPSFix(GPSFix gpsFix) {
        return add(gpsFix, /* replace */ true);
    }
    
    @Override
    public boolean add(GPSFix fix) {
        return super.add(fix); // ends up calling this.add(fix, false) where conversion in CompactGPSFixImpl will happen
    }

    @Override
    public boolean add(GPSFix fix, boolean replace) {
        return super.add(
                isLosslessCompaction() ? new PreciseCompactGPSFixImpl(fix) : new VeryCompactGPSFixImpl(fix), replace);
    }

    @Override
    public void setMillisecondsOverWhichToAverage(long millisecondsOverWhichToAverage) {
        super.setMillisecondsOverWhichToAverage(millisecondsOverWhichToAverage);
    }
}
