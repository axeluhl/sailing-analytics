package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.TimePoint;

public interface TrackedEvent {
    Event getEvent();

    Iterable<TrackedRace> getTrackedRaces();

    Iterable<TrackedRace> getTrackedRaces(BoatClass boatClass);

    /**
     * Obtains the tracked race for <code>race</code>. Blocks until the tracked race has been created
     * and added to this tracked event (see {@link #addTrackedRace(TrackedRace)}).
     */
    TrackedRace getTrackedRace(RaceDefinition race);

    /**
     * Non-blocking call that returns <code>null</code> if no tracking information currently exists
     * for <code>race</code>. See also {@link #getTrackedRace(RaceDefinition)} for a blocking variant.
     */
    TrackedRace getExistingTrackedRace(RaceDefinition race);
    
    void addTrackedRace(TrackedRace trackedRace);

    void removedTrackedRace(TrackedRace trackedRace);

    /**
     * Listener will be notified when {@link #addTrackedRace(TrackedRace)} is called and
     * upon registration for each tracked race already known. Therefore, the listener
     * won't miss any tracked race.
     */
    void addRaceListener(RaceListener listener);
    
    int getTotalPoints(Competitor competitor, TimePoint timePoint);
    
    int getNetPoints(Competitor competitor, TimePoint timePoint) throws NoWindException;

}