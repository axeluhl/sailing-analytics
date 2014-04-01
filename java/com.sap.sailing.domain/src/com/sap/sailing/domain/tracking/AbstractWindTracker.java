package com.sap.sailing.domain.tracking;

public abstract class AbstractWindTracker implements WindTracker {
    private final DynamicTrackedRace trackedRace;

    protected AbstractWindTracker(DynamicTrackedRace trackedRace) {
        super();
        this.trackedRace = trackedRace;
    }

    protected DynamicTrackedRace getTrackedRace() {
        return trackedRace;
    }

}
