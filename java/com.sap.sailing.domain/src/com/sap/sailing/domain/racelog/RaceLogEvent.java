package com.sap.sailing.domain.racelog;

import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Timed;
import com.sap.sailing.domain.common.WithID;

/**
 * Records an event that is sent by the race log via the Race Committee Cockpit App. The event is timed and may influence the state of a race depending on its context the the current
 * state of the RaceLog.
 * Subtypes may be flag events (AP, N, X, P, etc.) or operational race events
 *
 */
public interface RaceLogEvent extends Timed, WithID {
	List<Competitor> getInvolvedBoats();
	
	int getPassId();
	
	void accept(RaceLogEventVisitor visitor);
}
