package com.sap.sailing.domain.abstractlog.race.state.racingprocedure;

import java.util.Collection;

import com.sap.sailing.domain.abstractlog.race.state.RaceStateEvent;
import com.sap.sse.common.TimePoint;

/**
 * Base interface for all write-enabled {@link RacingProcedure}s.
 */
public interface RacingProcedure extends ReadonlyRacingProcedure {
    
    /**
     * Displays individual recall.
     */
    void displayIndividualRecall(TimePoint now);
    
    /**
     * Removes the individual recall flag.
     */
    void removeIndividualRecall(TimePoint now);

    /**
     * Delivers the time points and types of the events around the start, each optionally
     * leading to a change in race status.
     */
    Collection<RaceStateEvent> createStartStateEvents(TimePoint startTime);

}
