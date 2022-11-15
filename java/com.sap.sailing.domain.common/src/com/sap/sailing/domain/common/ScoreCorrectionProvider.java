package com.sap.sailing.domain.common;

import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import com.sap.sse.common.Named;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;

/**
 * Describes sources of official regatta results which may be imported and applied to <code>ScoreCorrection</code>
 * objects to update our leaderboards from the official scoring systems where necessary and applicable.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface ScoreCorrectionProvider extends Named {
    /**
     * Returns a valid map whose keys are event names such as "Kieler Woche 2011" or "IDM Travemuende 2011" and whose
     * values are sets of pairs of boat class names and the times for which it has score corrections. If there are multiple
     * score corrections taken at different times. Later score corrections are expected to be cumulative, meaning they
     * also contain all previous corrections.
     */
    Map<String, Set<Util.Pair<String, TimePoint>>> getHasResultsForBoatClassFromDateByEventName() throws Exception;

    /**
     * @param eventName
     *            as provided by the keys of the result map returned by
     *            {@link #getHasResultsForBoatClassFromDateByEventName()}
     * @param boatClassName
     *            as provided by the {@link Pair#getA()} method on the values of the map returned by
     *            {@link #getHasResultsForBoatClassFromDateByEventName()}
     * @param timePoint
     *            a time point as returned in the {@link Pair#getB()} component of the values in the map returned by
     *            {@link #getHasResultsForBoatClassFromDateByEventName()}.
     */
    RegattaScoreCorrections getScoreCorrections(String eventName, String boatClassName, TimePoint timePoint) throws Exception;
    
    /**
     * Produces a single {@link RegattaScoreCorrections} object from a single {@link InputStream}.
     */
    RegattaScoreCorrections getScoreCorrections(InputStream inputStream) throws Exception;
}
