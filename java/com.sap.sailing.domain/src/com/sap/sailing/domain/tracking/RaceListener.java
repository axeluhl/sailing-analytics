package com.sap.sailing.domain.tracking;

/**
 * Can be {@link TrackedRegatta#addRaceListener(RaceListener, java.util.Optional) added to}
 * and {@link TrackedRegatta#removeRaceListener(RaceListener) removed from} a {@link TrackedRegatta}
 * to listen for {@link TrackedRace}s being added and removed from the {@link TrackedRegatta}.<p>
 * 
 * The notifications are processed by a single thread per {@link TrackedRegatta}. This implies
 * that implementations must not block for events triggered only by other callbacks to implementations
 * of this interface, or else they risk a deadlock. For example, trying a blocking wait for
 * another {@link TrackedRace} to appear is a bad idea because the appearance of that other
 * race may have to be signalled by a {@link #raceAdded(TrackedRace)} callback. * 
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface RaceListener {
    void raceAdded(TrackedRace trackedRace);

    void raceRemoved(TrackedRace trackedRace);
}
