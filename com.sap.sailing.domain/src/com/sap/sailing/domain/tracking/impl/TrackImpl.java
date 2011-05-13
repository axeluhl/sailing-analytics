package com.sap.sailing.domain.tracking.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.TreeSet;

import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.Track;

public class TrackImpl<ItemType, FixType extends GPSFix> implements Track<ItemType, FixType> {
    private final ItemType trackedItem;
    
    /**
     * The fixes, ordered by their time points
     */
    final TreeSet<GPSFix> fixes;
    
    private class GPSFixByTimePointComparator implements Comparator<GPSFix> {
        @Override
        public int compare(GPSFix o1, GPSFix o2) {
            return o1.getTimePoint().compareTo(o2.getTimePoint());
        }
    }
    
    private class DummyGPSFixWithDateOnly implements GPSFix {
        private final TimePoint timePoint;
        public DummyGPSFixWithDateOnly(TimePoint timePoint) {
            super();
            this.timePoint = timePoint;
        }

        @Override
        public Position getPosition() {
            throw new UnsupportedOperationException();
        }

        @Override
        public TimePoint getTimePoint() {
            return timePoint;
        }
        
    }
    
    public TrackImpl(ItemType trackedItem) {
        super();
        this.trackedItem = trackedItem;
        this.fixes = new TreeSet<GPSFix>(new GPSFixByTimePointComparator());
    }

    @Override
    public ItemType getTrackedItem() {
        return trackedItem;
    }
    
    
    /**
     * Iterates the fixes in the order of their time points
     */
    @SuppressWarnings("unchecked")
    @Override
    public Iterable<FixType> getFixes() {
        return (Iterable<FixType>) Collections.unmodifiableSet(fixes);
    }

    @Override
    public Position getEstimatedPosition(TimePoint timePoint) {
        // TODO Auto-generated method stub
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public FixType getLastFixAtOrBefore(TimePoint timePoint) {
        return (FixType) fixes.floor(new DummyGPSFixWithDateOnly(timePoint));
    }

    @SuppressWarnings("unchecked")
    @Override
    public FixType getFirstFixAtOrAfter(TimePoint timePoint) {
        return (FixType) fixes.ceiling(new DummyGPSFixWithDateOnly(timePoint));
    }

    @SuppressWarnings("unchecked")
    @Override
    public FixType getLastFixBefore(TimePoint timePoint) {
        return (FixType) fixes.lower(new DummyGPSFixWithDateOnly(timePoint));
    }

    @SuppressWarnings("unchecked")
    @Override
    public FixType getFirstFixAfter(TimePoint timePoint) {
        return (FixType) fixes.higher(new DummyGPSFixWithDateOnly(timePoint));
    }

}
