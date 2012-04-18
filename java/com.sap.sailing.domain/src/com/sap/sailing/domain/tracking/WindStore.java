package com.sap.sailing.domain.tracking;

import java.io.Serializable;
import java.util.Map;

import com.sap.sailing.domain.common.WindSource;


/**
 * Capable of providing a {@link WindTrack} for a given event / race / {@link WindSource} combination.
 * A trivial implementation is to provide a new {@link WindTrack}. Other implementations may use
 * wind information stored persistently, e.g., in a database.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface WindStore extends Serializable {
    WindTrack getWindTrack(TrackedEvent trackedEvent, TrackedRace trackedRace, WindSource windSource,
            long millisecondsOverWhichToAverage, long delayForWindEstimationCacheInvalidation);

    /**
     * Loads all wind tracks known to this wind store that pertain to the tracked race / event specified.
     * 
     * @return a map that is never <code>null</code> but may be empty
     */
    Map<? extends WindSource, ? extends WindTrack> loadWindTracks(TrackedEvent trackedEvent,
            TrackedRace trackedRace, long millisecondsOverWhichToAverageWind);
}
