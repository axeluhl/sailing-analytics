package com.sap.sailing.domain.tracking;

import java.util.Set;

import com.sap.sailing.domain.base.Regatta;

/**
 * @author Alexander Ries (D062114)
 *
 */
public interface RaceExecutionOrderProvider {

    /**
     * Parameter <code>race</code> is a {@link TrackedRace} from which the method returns the previous
     * {@link TrackedRace}s in the execution order of a {@link Regatta}. This expresses the fact that the races returned
     * by this method must all have been finished before <code>race</code> can start.
     */
    Set<TrackedRace> getPreviousRaceInExecutionOrder(TrackedRace race);
}
