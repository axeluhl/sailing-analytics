package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.Named;
import com.sap.sailing.domain.tracking.TrackedEvent;

/**
 * The name shall be unique across all events tracked concurrently. In particular, if you want to
 * keep apart events in different boat classes, make sure the boat class name becomes part of the
 * event name.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface Event extends Named {
    /**
     * Please note that the {@link RaceDefinition}s of the {@link Event} are not necessarily in sync with the
     * {@link TrackedRace}s of the {@link TrackedEvent} whose {@link TrackedEvent#getEvent() event} is this event.
     * For example, it may be the case that a {@link RaceDefinition} is returned by this method for which no
     * {@link TrackedRace} exists in the corresponding {@link TrackedEvent}. This could be the case, e.g., during
     * the initialization of the tracker as well as during removing a race from the server.<p>
     * 
     * Callers iterating over the result that anticipate concurrent modifications shall synchronize the iteration
     * on the result.
     */
    Iterable<RaceDefinition> getAllRaces();
    
    /**
     * Please note that the set of {@link RaceDefinition}s contained by this event may not match up with the 
     * {@link TrackedRace}s of the {@link TrackedEvent} corresponding to this event. See also {@link #getAllRaces()}.
     * 
     * @return <code>null</code>, if this event does not contain a race (see {@link #getAllRaces}) whose
     * {@link RaceDefinition#getName()} equals <code>raceName</code>
     */
    RaceDefinition getRaceByName(String raceName);
    
    BoatClass getBoatClass();
    
    Iterable<Competitor> getCompetitors();

    void addRace(RaceDefinition race);

    void removeRace(RaceDefinition raceDefinition);
 
    void addEventListener(EventListener listener);
    
    void removeEventListener(EventListener listener);
}
