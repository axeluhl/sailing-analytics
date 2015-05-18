package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sse.common.WithID;

/**
 * @author Alexander Ries (D062114)
 *
 */
public interface RaceExecutionOrderProvider extends WithID{

    /**
     * Parameter <code>race</code> is a {@link TrackedRace} from which the method returns the previous {@link TrackedRace} in the execution order of a {@link Regatta}.
     * */
    TrackedRace getPreviousRaceInExecutionOrder(TrackedRace race);
}
