package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.common.WindSource;


/**
 * Capable of providing a {@link WindTrack} for a given event / race / {@link WindSource} combination.
 * A trivial implementation is to provide a new {@link WindTrack}. Other implementations may use
 * wind information stored persistently, e.g., in a database.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface WindStore {
    WindTrack getWindTrack(TrackedEvent trackedEvent, TrackedRace trackedRace, WindSource windSource, long millisecondsOverWhichToAverage);
}
