package com.sap.sailing.dashboards.gwt.server.startlineadvantages.precalculation;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * @author Alexander Ries (D062114)
 *
 */
public interface PreCalculationWindRetriever {

    default Wind retrieveWindAtPosition(Position position, TrackedRace trackedRace) {
        Wind result = null;
        result = trackedRace.getWind(position, MillisecondsTimePoint.now());
        return result;
    }
}
