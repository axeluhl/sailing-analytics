package com.sap.sailing.domain.tracking;


public abstract class AbstractRaceTrackerImpl implements RaceTracker {
    private DynamicTrackedEvent trackedEvent;
    
    public AbstractRaceTrackerImpl() {
    }

    @Override
    public DynamicTrackedEvent getTrackedEvent() {
        return trackedEvent;
    }

    protected void setTrackedEvent(DynamicTrackedEvent trackedEvent) {
        this.trackedEvent = trackedEvent;
    }
}
