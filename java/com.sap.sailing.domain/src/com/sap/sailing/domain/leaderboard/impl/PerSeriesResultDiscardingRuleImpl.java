package com.sap.sailing.domain.leaderboard.impl;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.ResultDiscardingRule;
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
            Iterable<RaceColumn> raceColumnsToConsider, TimePoint timePoint) {
        Set<RaceColumn> result = new HashSet<>();
        for (Series s : regatta.getSeries()) {
            if (s.getResultDiscardingRule() != null) {
                Iterable<RaceColumn> columnsToConsiderInSeries = getColumnsToConsiderInSeries(s, raceColumnsToConsider);
                result.addAll(s.getResultDiscardingRule().getDiscardedRaceColumns(competitor, leaderboard, columnsToConsiderInSeries, timePoint));
            }
        }
        return result;
    }

    private Iterable<RaceColumn> getColumnsToConsiderInSeries(Series series, Iterable<RaceColumn> allRaceColumnsToConsider) {
        Set<RaceColumn> result = new HashSet<>();
        for (RaceColumn seriesColumn : series.getRaceColumns()) {
            if (Util.contains(allRaceColumnsToConsider, seriesColumn)) {
                result.add(seriesColumn);
            }
        }
        return result;
    }

}
