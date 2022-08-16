package com.sap.sailing.domain.leaderboard.impl;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.ResultDiscardingRule;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.leaderboard.caching.LeaderboardDTOCalculationReuseCache;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingAndORCPerformanceCurveCache;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

/**
 * A result discarding rule that is used when one or more of a {@link Regatta}'s {@link Series} define their own
 * result discarding rule. In this case, discards are decided per series and not per regatta or leaderboard.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class PerSeriesResultDiscardingRuleImpl implements ResultDiscardingRule {
    private static final long serialVersionUID = 4596442640424592181L;

    private final Regatta regatta;
    
    public PerSeriesResultDiscardingRuleImpl(Regatta regatta) {
        super();
        this.regatta = regatta;
    }

    @Override
    public Set<RaceColumn> getDiscardedRaceColumns(Competitor competitor, Leaderboard leaderboard,
            Iterable<RaceColumn> raceColumnsToConsider, TimePoint timePoint, ScoringScheme scoringScheme) {
        return getDiscardedRaceColumns(competitor, leaderboard, raceColumnsToConsider, timePoint, scoringScheme, new LeaderboardDTOCalculationReuseCache(timePoint));
    }

    @Override
    public Set<RaceColumn> getDiscardedRaceColumns(Competitor competitor, Leaderboard leaderboard,
            Iterable<RaceColumn> raceColumnsToConsider, TimePoint timePoint,
            ScoringScheme scoringScheme, Function<RaceColumn, Double> totalPointsSupplier,
            WindLegTypeAndLegBearingAndORCPerformanceCurveCache cache) {
        final Set<RaceColumn> result = new HashSet<>();
        for (final Series s : regatta.getSeries()) {
            if (s.getResultDiscardingRule() != null) {
                final Iterable<RaceColumn> columnsToConsiderInSeries = getColumnsToConsiderInSeries(s, raceColumnsToConsider);
                result.addAll(s.getResultDiscardingRule().getDiscardedRaceColumns(competitor, leaderboard, columnsToConsiderInSeries,
                        timePoint, scoringScheme, totalPointsSupplier, cache));
            }
        }
        return result;
    }

    private Iterable<RaceColumn> getColumnsToConsiderInSeries(Series series, Iterable<RaceColumn> allRaceColumnsToConsider) {
        final Set<RaceColumn> result = new LinkedHashSet<>();
        for (final RaceColumn seriesColumn : series.getRaceColumns()) {
            if (Util.contains(allRaceColumnsToConsider, seriesColumn)) {
                result.add(seriesColumn);
            }
        }
        return result;
    }
}
