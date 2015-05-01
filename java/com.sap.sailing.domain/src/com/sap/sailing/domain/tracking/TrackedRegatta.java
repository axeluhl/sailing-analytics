package com.sap.sailing.domain.tracking;

import java.io.Serializable;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Sideline;
import com.sap.sailing.domain.base.impl.TrackedRaces;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.racelog.tracking.GPSFixStore;
import com.sap.sse.common.TimePoint;

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

    Iterable<? extends TrackedRace> getTrackedRaces();

    Iterable<TrackedRace> getTrackedRaces(BoatClass boatClass);

    /**
     * Creates a {@link TrackedRace} based on the parameter specified and {@link #addTrackedRace(TrackedRace) adds} it
     * to this tracked regatta. Afterwards, calling {@link #getTrackedRace(RaceDefinition) getTrackedRace(raceDefinition)}
     * will return the result of this method call.
     * 
     * @param raceDefinitionSetToUpdate
     *            if not <code>null</code>, after creating the {@link TrackedRace}, the <code>raceDefinition</code> is
     *            {@link DynamicRaceDefinitionSet#addRaceDefinition(RaceDefinition, DynamicTrackedRace) added} to that object.
     */
    DynamicTrackedRace createTrackedRace(RaceDefinition raceDefinition, Iterable<Sideline> sidelines, WindStore windStore,
    		GPSFixStore gpsFixStore, long delayToLiveInMillis, long millisecondsOverWhichToAverageWind, long millisecondsOverWhichToAverageSpeed,
            DynamicRaceDefinitionSet raceDefinitionSetToUpdate, boolean useInternalMarkPassingAlgorithm);

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
    
    void addTrackedRace(TrackedRace trackedRace);

    void removeTrackedRace(TrackedRace trackedRace);

    /**
     * Listener will be notified when {@link #addTrackedRace(TrackedRace)} is called and
     * upon registration for each tracked race already known. Therefore, the listener
     * won't miss any tracked race.
     */
    void addRaceListener(RaceListener listener);
    
    int getNetPoints(Competitor competitor, TimePoint timePoint) throws NoWindException;

    void removeTrackedRace(RaceDefinition raceDefinition);
    
    /**
     * Parameter <code>race</code> is a {@link TrackedRace} from which the method returns the previous {@link TrackedRace} in the execution order of a {@link Regatta}.
     * */
    TrackedRace getPreviousRaceInExecutionOrder(TrackedRace race);
    
}