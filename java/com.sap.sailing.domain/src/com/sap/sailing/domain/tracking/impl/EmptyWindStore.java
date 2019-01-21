package com.sap.sailing.domain.tracking.impl;

import java.util.Collections;
import java.util.Map;

import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tracking.WindTrack;

public class EmptyWindStore implements WindStore {
    public static EmptyWindStore INSTANCE = new EmptyWindStore();
    
    @Override
    public WindTrack getWindTrack(String regattaName, TrackedRace trackedRace, WindSource windSource,
            long millisecondsOverWhichToAverage, long delayForWindEstimationCacheInvalidation) {
        switch (windSource.getType()) {
        case COURSE_BASED:
            return new CourseBasedWindTrackImpl(trackedRace, millisecondsOverWhichToAverage, WindSourceType.COURSE_BASED.getBaseConfidence());
        case TRACK_BASED_ESTIMATION:
            return new TrackBasedEstimationWindTrackImpl(trackedRace, millisecondsOverWhichToAverage,
                    WindSourceType.TRACK_BASED_ESTIMATION.getBaseConfidence(), delayForWindEstimationCacheInvalidation);
        case MANEUVER_BASED_ESTIMATION:
            // TODO devise suitable osgi bundle architecture to access windestimation
            // return new InteractiveMstHmmWindEstimationForTrackedRace(trackedRace, windSource, millisecondsOverWhichToAverage);
            return new WindTrackImpl(millisecondsOverWhichToAverage, false, "Dummy");
        default:
            return new WindTrackImpl(millisecondsOverWhichToAverage, windSource.getType().useSpeed(), "WindTrack for source "+windSource);
        }
    }

    @Override
    public Map<? extends WindSource, ? extends WindTrack> loadWindTracks(String regattaName,
            TrackedRace trackedRace, long millisecondsOverWhichToAverageWind) {
        return Collections.emptyMap();
    }

    @Override
    public void clear() {
    }
}
