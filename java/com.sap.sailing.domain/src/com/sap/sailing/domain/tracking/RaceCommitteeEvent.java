package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.Timed;

/**
 * Records an event that is sent by the race committee via the Race Committee Cockpit App. The event is timed and may influence the state of a race depending on its context the the current
 * state of the RaceCommitteeEventTrack.
 * Subtypes may be flag events (AP, N, X, P, etc.) or operational race events
 *
 */
public interface RaceCommitteeEvent extends Timed {

}
