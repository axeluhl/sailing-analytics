package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.abstractlog.race.CompetitorResult;
import com.sap.sailing.domain.abstractlog.race.CompetitorResults;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogFinishPositioningEvent;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.AbstractFinishPositioningListFinder.CompetitorResultsAndTheirCreationTimePoints;
import com.sap.sailing.domain.abstractlog.race.impl.CompetitorResultsImpl;
import com.sap.sse.common.TimePoint;

/**
 * Fetches competitor results from the current pass in a {@link RaceLog}. Results may be provided in multiple, partial
 * race log events, all of potentially different priority, with time stamps and potential overlaps, providing different
 * results for the same competitor at different times, with different priorities. This finder merges all these results
 * from the current pass and prefers higher priorities (numerically less) over lower priorities (numerically greater),
 * and later results over earlier results with equal priority. It can therefore rely on the order in which race log
 * events are produced by {@link #getPassEventsDescending()}.
 * <p>
 * 
 * The analyzer will always return a valid {@link CompetitorResultsAndTheirCreationTimePoints} object which may have
 * {@code null} for its {@link CompetitorResultsAndTheirCreationTimePoints#getCompetitorResults()} return if no result
 * has been found at all.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class AbstractFinishPositioningListFinder extends RaceLogAnalyzer<CompetitorResultsAndTheirCreationTimePoints> {
    /**
     * The type of event we're looking for in the log
     */
    private final Class<? extends RaceLogFinishPositioningEvent> clz;
    
    public static class CompetitorResultsAndTheirCreationTimePoints {
        private final CompetitorResults competitorResults;
        private final Map<Serializable, TimePoint> resultCreationTimePointByCompetitorId;

        public CompetitorResultsAndTheirCreationTimePoints(CompetitorResults competitorResults,
                Map<Serializable, TimePoint> resultCreationTimePointByCompetitorId) {
            super();
            this.competitorResults = competitorResults;
            this.resultCreationTimePointByCompetitorId = resultCreationTimePointByCompetitorId;
        }

        public CompetitorResults getCompetitorResults() {
            return competitorResults;
        }

        /**
         * For every competitor result in {@link #getCompetitorResults()}, if you take its
         * {@link CompetitorResult#getCompetitorId() competitor ID} and pass it as parameter to this method you can be
         * guaranteed to obtain a non-{@code null} result that tells the creation time point of the race log event that
         * contains the result of the competitor identified this way.
         */
        public TimePoint getCreationTimePointOfResultForCompetitorWithId(Serializable competitorId) {
            return resultCreationTimePointByCompetitorId.get(competitorId);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((competitorResults == null) ? 0 : competitorResults.hashCode());
            result = prime * result + ((resultCreationTimePointByCompetitorId == null) ? 0
                    : resultCreationTimePointByCompetitorId.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            CompetitorResultsAndTheirCreationTimePoints other = (CompetitorResultsAndTheirCreationTimePoints) obj;
            if (competitorResults == null) {
                if (other.competitorResults != null)
                    return false;
            } else if (!competitorResults.equals(other.competitorResults))
                return false;
            if (resultCreationTimePointByCompetitorId == null) {
                if (other.resultCreationTimePointByCompetitorId != null)
                    return false;
            } else if (!resultCreationTimePointByCompetitorId.equals(other.resultCreationTimePointByCompetitorId))
                return false;
            return true;
        }
    }

    public AbstractFinishPositioningListFinder(RaceLog raceLog, Class<? extends RaceLogFinishPositioningEvent> clz) {
        super(raceLog);
        this.clz = clz;
    }

    @Override
    protected CompetitorResultsAndTheirCreationTimePoints performAnalysis() {
        boolean resultFound = false;
        final Map<Serializable, CompetitorResult> resultsByCompetitor = new HashMap<>();
        final Map<Serializable, TimePoint> resultCreationTimePointsByCompetitorId = new HashMap<>();
        for (RaceLogEvent event : getPassEventsDescending()) {
            if (clz.isInstance(event)) {
                RaceLogFinishPositioningEvent finishPositioningEvent = (RaceLogFinishPositioningEvent) event;
                final CompetitorResults positionedCompetitorsIDsNamesMaxPointsReasons = finishPositioningEvent.getPositionedCompetitorsIDsNamesMaxPointsReasons();
                final TimePoint creationTimePoint = finishPositioningEvent.getCreatedAt();
                if (positionedCompetitorsIDsNamesMaxPointsReasons != null) {
                    resultFound = true;
                    for (final CompetitorResult r : positionedCompetitorsIDsNamesMaxPointsReasons) {
                        if (!resultsByCompetitor.containsKey(r.getCompetitorId())) {
                            resultsByCompetitor.put(r.getCompetitorId(), r);
                            resultCreationTimePointsByCompetitorId.put(r.getCompetitorId(), creationTimePoint);
                        }
                    }
                }
            }
        }
        final CompetitorResultsImpl result = new CompetitorResultsImpl();
        result.addAll(resultsByCompetitor.values());
        return new CompetitorResultsAndTheirCreationTimePoints(resultFound ? result : null, resultCreationTimePointsByCompetitorId);
    }
}
