package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.abstractlog.race.CompetitorResult;
import com.sap.sailing.domain.abstractlog.race.CompetitorResults;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogFinishPositioningEvent;
import com.sap.sailing.domain.abstractlog.race.impl.CompetitorResultsImpl;

/**
 * Fetches competitor results from the current pass in a {@link RaceLog}. Results may be provided in multiple, partial
 * race log events, all of potentially different priority, with time stamps and potential overlaps, providing different
 * results for the same competitor at different times, with different priorities. This finder merges all these results
 * from the current pass and prefers higher priorities (numerically less) over lower priorities (numerically greater),
 * and later results over earlier results with equal priority. It can therefore rely on the order in which race log
 * events are produced by {@link #getPassEventsDescending()}.<p>
 * 
 * The analyzer will return {@code null} if no result has been found at all.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class AbstractFinishPositioningListFinder extends RaceLogAnalyzer<CompetitorResults> {
    /**
     * The type of event we're looking for in the log
     */
    private final Class<? extends RaceLogFinishPositioningEvent> clz;

    public AbstractFinishPositioningListFinder(RaceLog raceLog, Class<? extends RaceLogFinishPositioningEvent> clz) {
        super(raceLog);
        this.clz = clz;
    }

    @Override
    protected CompetitorResults performAnalysis() {
        boolean resultFound = false;
        final Map<Serializable, CompetitorResult> resultsByCompetitor = new HashMap<>();
        for (RaceLogEvent event : getPassEventsDescending()) {
            if (clz.isInstance(event)) {
                RaceLogFinishPositioningEvent finishPositioningEvent = (RaceLogFinishPositioningEvent) event;
                final CompetitorResults positionedCompetitorsIDsNamesMaxPointsReasons = finishPositioningEvent.getPositionedCompetitorsIDsNamesMaxPointsReasons();
                if (positionedCompetitorsIDsNamesMaxPointsReasons != null) {
                    resultFound = true;
                    for (final CompetitorResult r : positionedCompetitorsIDsNamesMaxPointsReasons) {
                        if (!resultsByCompetitor.containsKey(r.getCompetitorId())) {
                            resultsByCompetitor.put(r.getCompetitorId(), r);
                        }
                    }
                }
            }
        }
        final CompetitorResultsImpl result = new CompetitorResultsImpl();
        result.addAll(resultsByCompetitor.values());
        return resultFound ? result : null;
    }
}
