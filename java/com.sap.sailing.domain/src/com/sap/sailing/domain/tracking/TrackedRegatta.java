package com.sap.sailing.domain.tracking;

import java.io.Serializable;
import java.util.Optional;
import java.util.concurrent.Future;

import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Sideline;
import com.sap.sailing.domain.base.impl.TrackedRaces;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sse.common.TimePoint;
import com.sap.sse.util.ThreadLocalTransporter;

/**
 * Manages a set of {@link TrackedRace} objects that belong to the same {@link Regatta} (regatta, sailing regatta for a
 * single boat class). It therefore represents the entry point into the tracking-related objects for such an regatta.
 * Allows clients to find a {@link TrackedRace} by the {@link RaceDefinition} for which it holds the tracking data.
 * <p>
 * 
 * Please note that the result of calling {@link #getRegatta()}.{@link Regatta#getAllRaces() getAllRaces()} is not
 * guaranteed to match up with the races obtained by calling {@link TrackedRace#getRace()} on all {@link TrackedRaces}
 * resulting from {@link #getTrackedRaces()}. In other words, the processes for adding and removing races to the
 * server do not guarantee to update the master and tracking data for races atomically.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public interface TrackedRegatta extends Serializable {
    Regatta getRegatta();

    /**
     * Callers must {@link #lockTrackedRacesForRead() acquire the read lock} before calling this method and hold on to the lock
     * while iterating over the data structure returned. Example:
     * <pre>
     *     trackedRegatta.lockTrackedRacesForRead();
     *     try {
     *         for (TrackedRace trackedRace : trackedRegatta.getTrackedRaces()) {
     *             // do something
     *         }
     *     } finally {
     *         trackedRegatta.unlockTrackedRacesAfterRead();
     *     }
     * </pre>
     * The method will throw an {@link IllegalArgumentException} if the caller fails to do so.
     */
    Iterable<? extends TrackedRace> getTrackedRaces();

    void lockTrackedRacesForRead();
    
    void unlockTrackedRacesAfterRead();
    
    void lockTrackedRacesForWrite();

    void unlockTrackedRacesAfterWrite();

    /**
     * Creates a {@link TrackedRace} based on the parameter specified and {@link #addTrackedRace(TrackedRace) adds} it
     * to this tracked regatta. Afterwards, calling {@link #getTrackedRace(RaceDefinition) getTrackedRace(raceDefinition)}
     * will return the result of this method call.
     * @param raceDefinitionSetToUpdate
     *            if not <code>null</code>, after creating the {@link TrackedRace}, the <code>raceDefinition</code> is
     *            {@link DynamicRaceDefinitionSet#addRaceDefinition(RaceDefinition, DynamicTrackedRace) added} to that object.
     * @param raceLogResolver TODO
     */
    DynamicTrackedRace createTrackedRace(RaceDefinition raceDefinition, Iterable<Sideline> sidelines, WindStore windStore,
            long delayToLiveInMillis, long millisecondsOverWhichToAverageWind, long millisecondsOverWhichToAverageSpeed,
            DynamicRaceDefinitionSet raceDefinitionSetToUpdate, boolean useInternalMarkPassingAlgorithm, RaceLogResolver raceLogResolver,
            Optional<ThreadLocalTransporter> beforeAndAfterNotificationHandler);

    /**
     * Obtains the tracked race for <code>race</code>. Blocks until the tracked race has been created
     * and added to this tracked regatta (see {@link #addTrackedRace(TrackedRace)}).
     */
    TrackedRace getTrackedRace(RaceDefinition race);

    /**
     * Non-blocking call that returns <code>null</code> if no tracking information currently exists
     * for <code>race</code>. See also {@link #getTrackedRace(RaceDefinition)} for a blocking variant.
     */
    TrackedRace getExistingTrackedRace(RaceDefinition race);
    
    void addTrackedRace(TrackedRace trackedRace, Optional<ThreadLocalTransporter> beforeAndAfterNotificationHandler);

    void removeTrackedRace(TrackedRace trackedRace, Optional<ThreadLocalTransporter> beforeAndAfterNotificationHandler);

    /**
     * Listener will be notified when {@link #addTrackedRace(TrackedRace)} is called and
     * upon registration for each tracked race already known. Therefore, the listener
     * won't miss any tracked race.
     */
    void addRaceListener(RaceListener listener, Optional<ThreadLocalTransporter> beforeAndAfterNotificationHandler);
    
    /**
     * Removes the given listener and returns a {@link Future} that will be completed
     * when it is guaranteed that no more events will be fired to the listener.
     */
    Future<Boolean> removeRaceListener(RaceListener listener);

    int getTotalPoints(Competitor competitor, TimePoint timePoint) throws NoWindException;

}