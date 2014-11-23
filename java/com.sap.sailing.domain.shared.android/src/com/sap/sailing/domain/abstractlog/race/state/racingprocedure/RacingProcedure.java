package com.sap.sailing.domain.abstractlog.race.state.racingprocedure;

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

}
