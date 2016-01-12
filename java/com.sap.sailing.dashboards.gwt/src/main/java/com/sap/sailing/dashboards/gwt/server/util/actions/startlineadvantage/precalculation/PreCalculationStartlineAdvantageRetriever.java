package com.sap.sailing.dashboards.gwt.server.util.actions.startlineadvantage.precalculation;

import com.sap.sailing.domain.tracking.LineDetails;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * @author Alexander Ries (D062114)
 *
 */
public interface PreCalculationStartlineAdvantageRetriever {

    default double retrieveStartlineAdvantage(TrackedRace trackedRace) {
        double result = 0;
        LineDetails startline = trackedRace.getStartLine(MillisecondsTimePoint.now());
        if (startline != null && startline.getAdvantage() != null) {
            result = startline.getAdvantage().getMeters();
        }
        return result;
    }

    default double retrieveStartlineLenght(TrackedRace trackedRace) {
        double result = 0;
        LineDetails startline = trackedRace.getStartLine(MillisecondsTimePoint.now());
        result = startline.getLength().getMeters();
        return result;
    }
}
