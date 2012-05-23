package com.sap.sailing.domain.leaderboard.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnInSeries;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.impl.RaceColumnInSeriesImpl;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.leaderboard.SettableScoreCorrection;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;

/**
 * A leaderboard that is based on the definition of a {@link Regatta} with its {@link Series} and {@link Fleet}.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class RegattaLeaderboardImpl extends AbstractLeaderboardImpl {
    private static final long serialVersionUID = 2370461218294770084L;
    private final Regatta regatta;

    public RegattaLeaderboardImpl(Regatta regatta, SettableScoreCorrection scoreCorrection, ThresholdBasedResultDiscardingRule resultDiscardingRule) {
        super(scoreCorrection, resultDiscardingRule);
        this.regatta = regatta;
    }
    
    private Regatta getRegatta() {
        return regatta;
    }

    @Override
    public String getName() {
        return getRegatta().getName();
    }

    @Override
    public Iterable<RaceColumn> getRaceColumns() {
        List<RaceColumn> result = new ArrayList<RaceColumn>();
        for (Series series : getRegatta().getSeries()) {
            for (RaceColumn raceColumn : series.getRaceColumns()) {
                result.add(raceColumn);
            }
        }
        return result;
    }

    @Override
    public RaceColumnInSeries getRaceColumnByName(String columnName) {
        return (RaceColumnInSeriesImpl) super.getRaceColumnByName(columnName);
    }

    protected List<Competitor> getCompetitorsFromBestToWorst(final RaceColumn raceColumn, TimePoint timePoint, final boolean isFleetsOrdered) throws NoWindException {
        final Map<Competitor, Pair<Integer, Integer>> netPointsAndFleetIndex = new HashMap<Competitor, Pair<Integer, Integer>>();
        for (Competitor competitor : getCompetitors()) {
            int netPoints = getNetPoints(competitor, raceColumn, timePoint);
            if (netPoints != 0) {
                netPointsAndFleetIndex.put(competitor, new Pair<Integer, Integer>(netPoints,
                        Util.indexOf(raceColumn.getFleets(), raceColumn.getFleetOfCompetitor(competitor))));
            }
        }
        List<Competitor> result = new ArrayList<Competitor>(netPointsAndFleetIndex.keySet());
        Collections.sort(result, new Comparator<Competitor>() {
            @Override
            public int compare(Competitor o1, Competitor o2) {
                int comparisonResult;
                if (o1 == o2) {
                    comparisonResult = 0;
                } else {
                    comparisonResult = 0;
                    if (isFleetsOrdered) {
                        comparisonResult = netPointsAndFleetIndex.get(o1).getB() - netPointsAndFleetIndex.get(o2).getB();
                    }
                    if (comparisonResult == 0) {
                        comparisonResult = netPointsAndFleetIndex.get(o1).getA() - netPointsAndFleetIndex.get(o2).getA();
                    }
                }
                return comparisonResult;
            }
        });
        return result;
    }

    /**
     * If the column has {@link Series#isFleetsOrdered() unordered fleets}, all competitors with non-zero net points
     * are added to the result which is then sorted by net points in ascending order. For ordered fleets, the fleet
     * is the primary ordering criterion, followed by the net points.
     */
    @Override
    public List<Competitor> getCompetitorsFromBestToWorst(final RaceColumn raceColumn, TimePoint timePoint) throws NoWindException {
        assert raceColumn instanceof RaceColumnInSeries;
        final RaceColumnInSeries raceColumnInSeries = (RaceColumnInSeries) raceColumn;
        return getCompetitorsFromBestToWorst(raceColumnInSeries, timePoint, raceColumnInSeries.getSeries().isFleetsOrdered());
    }

    @Override
    public List<Competitor> getCompetitorsFromBestToWorst(TimePoint timePoint) {
        // TODO Auto-generated method stub
        return null;
    }

}
