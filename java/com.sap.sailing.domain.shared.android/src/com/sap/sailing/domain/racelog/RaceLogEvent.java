package com.sap.sailing.domain.racelog;

import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Timed;
import com.sap.sailing.domain.common.WithID;

/**
 * Records an event that is sent by the race log via the Race Committee Cockpit App. 
 * The event is timed and may influence the state of a race depending on its context 
 * the the current state of the RaceLog.
 * 
 * Subclasses may be flag events (AP, N, X, P, etc.) or operational race events.
 *
 */
public interface RaceLogEvent extends Timed, WithID {

    /**
     * Gets the event's pass identifier.
     * 
     * Each {@link RaceLogEvent} is associated to a certain pass. A pass
     * is every attempt to start and run a race. A new pass is initiated
     * when a new start time is proposed (e.g. after the race was aborted).
     * 
     */
    int getPassId();

    /**
     * Gets the list of associated {@link Competitor}s.
     * 
     * A {@link RaceLogEvent} might be associated with a list of competitors,
     * which are somehow relevant for this kind of event. An example is a list
     * of competitors who are marked for an individual recall.
     */
    List<Competitor> getInvolvedBoats();

    /**
     * Visitor pattern to implement certain {@link RaceLogEvent} subclass specific
     * behavior.
     */
    void accept(RaceLogEventVisitor visitor);
}
