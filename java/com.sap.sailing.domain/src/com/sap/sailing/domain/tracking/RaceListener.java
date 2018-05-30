package com.sap.sailing.domain.tracking;

/**
 * Can be {@link TrackedRegatta#addRaceListener(RaceListener, java.util.Optional, boolean) added to} and
 * {@link TrackedRegatta#removeRaceListener(RaceListener) removed from} a {@link TrackedRegatta} to listen for
 * {@link TrackedRace}s being added and removed from the {@link TrackedRegatta}.
 * <p>
 * 
 * The notifications are processed by a single thread per {@link TrackedRegatta} and {@link RaceListener}. This implies
 * that implementations must not block for events triggered only by other events received by the same listener, or else
 * they risk a deadlock.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface RaceListener {
    void raceAdded(TrackedRace trackedRace);

    void raceRemoved(TrackedRace trackedRace);
}
