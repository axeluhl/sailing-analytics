package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.tracking.TrackedEvent;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindSource;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tracking.WindTrack;

public class EmptyWindStore implements WindStore {
    public static EmptyWindStore INSTANCE = new EmptyWindStore();
    
    @Override
    public WindTrack getWindTrack(TrackedEvent trackedEvent, TrackedRace trackedRace, WindSource windSource,
            long millisecondsOverWhichToAverage) {
        return new WindTrackImpl(millisecondsOverWhichToAverage);
    }

}
