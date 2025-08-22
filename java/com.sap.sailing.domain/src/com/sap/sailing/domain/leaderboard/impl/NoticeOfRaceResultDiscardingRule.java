package com.sap.sailing.domain.leaderboard.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnInSeries;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.ResultDiscardingRule;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.leaderboard.caching.LeaderboardDTOCalculationReuseCache;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingAndORCPerformanceCurveCache;
import com.sap.sse.common.TimePoint;

/**
 * Custom result discarding rule implementing the complex discard requirements from Notice of Race section 24.4:
 * 
 * 24.4.1 Single Fleet:
 * - 1-2 races: 0 discards
 * - 3-7 races: 1 discard
 * - 8-15 races: 2 discards
 * - 16+ races: 3 discards
 * - No more than one BFD score in Sprint Racing may be excluded
 * 
 * 24.4.2 Split Fleet:
 * - Qualifying Series: 1-2 races: 0 discards, 3-7 races: 1 discard, 8-10 races: 2 discards
 * - Final Series: 1-2 races: 0 discards, 3-7 races: 1 discard, 8+ races: 2 discards
 * - No more than one BFD score in Sprint Racing may be excluded from each series
 * 
 * @author Generated for Notice of Race Complex Scoring
 */
public class NoticeOfRaceResultDiscardingRule implements ResultDiscardingRule {
    private static final long serialVersionUID = 1L;
    
    // Single fleet discard thresholds: [races needed for 1 discard, 2 discards, 3 discards]
    private static final int[] SINGLE_FLEET_THRESHOLDS = {3, 8, 16};
    
    // Split fleet qualifying series thresholds: [races needed for 1 discard, 2 discards]
    private static final int[] QUALIFYING_SERIES_THRESHOLDS = {3, 8};
    
    // Split fleet final series thresholds: [races needed for 1 discard, 2 discards]
    private static final int[] FINAL_SERIES_THRESHOLDS = {3, 8};

    @Override
    public Set<RaceColumn> getDiscardedRaceColumns(Competitor competitor, Leaderboard leaderboard,
            Iterable<RaceColumn> raceColumnsToConsider, TimePoint timePoint, ScoringScheme scoringScheme) {
        return getDiscardedRaceColumns(competitor, leaderboard, raceColumnsToConsider, timePoint, scoringScheme,
                new LeaderboardDTOCalculationReuseCache(timePoint));
    }

    @Override
    public Set<RaceColumn> getDiscardedRaceColumns(Competitor competitor, Leaderboard leaderboard,
            Iterable<RaceColumn> raceColumnsToConsider, TimePoint timePoint, ScoringScheme scoringScheme,
            Function<RaceColumn, Double> totalPointsSupplier, WindLegTypeAndLegBearingAndORCPerformanceCurveCache cache) {
        
        // Determine if we have split fleets by checking if any series exists
        boolean hasSplitFleets = hasSplitFleetStructure(raceColumnsToConsider);
        
        if (hasSplitFleets) {
            return getDiscardedRaceColumnsForSplitFleet(competitor, leaderboard, raceColumnsToConsider, 
                    timePoint, scoringScheme, totalPointsSupplier, cache);
        } else {
            return getDiscardedRaceColumnsForSingleFleet(competitor, leaderboard, raceColumnsToConsider, 
                    timePoint, scoringScheme, totalPointsSupplier, cache);
        }
    }

    /**
     * Check if we have a split fleet structure (multiple series).
     */
    private boolean hasSplitFleetStructure(Iterable<RaceColumn> raceColumnsToConsider) {
        Set<Series> seriesFound = new HashSet<>();
        for (RaceColumn column : raceColumnsToConsider) {
            if (column instanceof RaceColumnInSeries) {
                seriesFound.add(((RaceColumnInSeries) column).getSeries());
                if (seriesFound.size() > 1) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Handle discards for single fleet format (24.4.1).
     */
    private Set<RaceColumn> getDiscardedRaceColumnsForSingleFleet(Competitor competitor, Leaderboard leaderboard,
            Iterable<RaceColumn> raceColumnsToConsider, TimePoint timePoint, ScoringScheme scoringScheme,
            Function<RaceColumn, Double> totalPointsSupplier, WindLegTypeAndLegBearingAndORCPerformanceCurveCache cache) {
        
        int numberOfRaces = countValidRaces(competitor, raceColumnsToConsider, leaderboard, timePoint);
        int numberOfDiscards = getNumberOfDiscardsForSingleFleet(numberOfRaces);
        
        if (numberOfDiscards == 0) {
            return Collections.emptySet();
        }
        
        return selectWorstRacesToDiscard(competitor, leaderboard, raceColumnsToConsider, timePoint, 
                scoringScheme, totalPointsSupplier, numberOfDiscards, 1); // Max 1 BFD in Sprint Racing
    }

    /**
     * Handle discards for split fleet format (24.4.2).
     */
    private Set<RaceColumn> getDiscardedRaceColumnsForSplitFleet(Competitor competitor, Leaderboard leaderboard,
            Iterable<RaceColumn> raceColumnsToConsider, TimePoint timePoint, ScoringScheme scoringScheme,
            Function<RaceColumn, Double> totalPointsSupplier, WindLegTypeAndLegBearingAndORCPerformanceCurveCache cache) {
        
        Set<RaceColumn> result = new HashSet<>();
        
        // Group race columns by series
        Map<Series, List<RaceColumn>> raceColumnsBySeries = groupRaceColumnsBySeries(raceColumnsToConsider);
        
        for (Map.Entry<Series, List<RaceColumn>> entry : raceColumnsBySeries.entrySet()) {
            Series series = entry.getKey();
            List<RaceColumn> seriesColumns = entry.getValue();
            
            int numberOfRaces = countValidRaces(competitor, seriesColumns, leaderboard, timePoint);
            int numberOfDiscards = getNumberOfDiscardsForSplitFleetSeries(series, numberOfRaces);
            
            if (numberOfDiscards > 0) {
                Set<RaceColumn> seriesDiscards = selectWorstRacesToDiscard(competitor, leaderboard, seriesColumns, 
                        timePoint, scoringScheme, totalPointsSupplier, numberOfDiscards, 1); // Max 1 BFD per series
                result.addAll(seriesDiscards);
            }
        }
        
        return result;
    }

    /**
     * Group race columns by their series.
     */
    private Map<Series, List<RaceColumn>> groupRaceColumnsBySeries(Iterable<RaceColumn> raceColumnsToConsider) {
        Map<Series, List<RaceColumn>> result = new HashMap<>();
        
        for (RaceColumn column : raceColumnsToConsider) {
            Series series = null;
            if (column instanceof RaceColumnInSeries) {
                series = ((RaceColumnInSeries) column).getSeries();
            }
            
            result.computeIfAbsent(series, k -> new ArrayList<>()).add(column);
        }
        
        return result;
    }

    /**
     * Get number of discards for single fleet based on Notice of Race 24.4.1.
     */
    private int getNumberOfDiscardsForSingleFleet(int numberOfRaces) {
        if (numberOfRaces >= SINGLE_FLEET_THRESHOLDS[2]) { // 16+
            return 3;
        } else if (numberOfRaces >= SINGLE_FLEET_THRESHOLDS[1]) { // 8-15
            return 2;
        } else if (numberOfRaces >= SINGLE_FLEET_THRESHOLDS[0]) { // 3-7
            return 1;
        } else { // 1-2
            return 0;
        }
    }

    /**
     * Get number of discards for split fleet series based on Notice of Race 24.4.2.
     */
    private int getNumberOfDiscardsForSplitFleetSeries(Series series, int numberOfRaces) {
        boolean isQualifyingSeries = isQualifyingSeries(series);
        
        if (isQualifyingSeries) {
            // Qualifying Series rules
            if (numberOfRaces >= QUALIFYING_SERIES_THRESHOLDS[1]) { // 8-10
                return 2;
            } else if (numberOfRaces >= QUALIFYING_SERIES_THRESHOLDS[0]) { // 3-7
                return 1;
            } else { // 1-2
                return 0;
            }
        } else {
            // Final Series rules
            if (numberOfRaces >= FINAL_SERIES_THRESHOLDS[1]) { // 8+
                return 2;
            } else if (numberOfRaces >= FINAL_SERIES_THRESHOLDS[0]) { // 3-7
                return 1;
            } else { // 1-2
                return 0;
            }
        }
    }

    /**
     * Determine if a series is a Qualifying Series based on naming convention.
     */
    private boolean isQualifyingSeries(Series series) {
        if (series == null || series.getName() == null) {
            return false;
        }
        
        String name = series.getName().toLowerCase();
        return name.contains("qualifying") || name.contains("qualification");
    }

    /**
     * Count valid races for discard calculation.
     */
    private int countValidRaces(Competitor competitor, Iterable<RaceColumn> raceColumns, 
            Leaderboard leaderboard, TimePoint timePoint) {
        int count = 0;
        for (RaceColumn column : raceColumns) {
            if (leaderboard.countRaceForComparisonWithDiscardingThresholds(competitor, column, timePoint)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Select the worst races to discard, with BFD limitations for Sprint Racing.
     */
    private Set<RaceColumn> selectWorstRacesToDiscard(Competitor competitor, Leaderboard leaderboard,
            Iterable<RaceColumn> raceColumns, TimePoint timePoint, ScoringScheme scoringScheme,
            Function<RaceColumn, Double> totalPointsSupplier, int numberOfDiscards, int maxBfdDiscards) {
        
        if (numberOfDiscards == 0) {
            return Collections.emptySet();
        }
        
        // Collect discardable races with their scores
        Map<RaceColumn, Double> raceScores = new HashMap<>();
        List<RaceColumn> discardableRaces = new ArrayList<>();
        
        for (RaceColumn column : raceColumns) {
            if (column.isDiscardable()) {
                Double totalPoints = totalPointsSupplier.apply(column);
                Double scaledScore = totalPoints == null ? null : scoringScheme.getScoreScaledByFactor(column, totalPoints);
                raceScores.put(column, scaledScore);
                discardableRaces.add(column);
            }
        }
        
        // Sort races by score (worst first)
        Collections.sort(discardableRaces, new Comparator<RaceColumn>() {
            @Override
            public int compare(RaceColumn rc1, RaceColumn rc2) {
                // Invert comparison to get worst scores first
                // Treat null scores as "better" so they end up at the end
                return -scoringScheme.getScoreComparator(true).compare(raceScores.get(rc1), raceScores.get(rc2));
            }
        });
        
        // Select races to discard, respecting BFD limitations
        Set<RaceColumn> result = new HashSet<>();
        int bfdDiscardsUsed = 0;
        
        Iterator<RaceColumn> iter = discardableRaces.iterator();
        while (iter.hasNext() && result.size() < numberOfDiscards) {
            RaceColumn column = iter.next();
            MaxPointsReason maxPointsReason = leaderboard.getMaxPointsReason(competitor, column, timePoint);
            
            if (maxPointsReason == null || maxPointsReason.isDiscardable()) {
                // Check BFD limitation for Sprint Racing
                if (maxPointsReason == MaxPointsReason.BFD && isSprintRacing(column)) {
                    if (bfdDiscardsUsed < maxBfdDiscards) {
                        result.add(column);
                        bfdDiscardsUsed++;
                    }
                    // Skip this BFD if we've already used our limit
                } else {
                    // Not a BFD or not Sprint Racing, can discard normally
                    result.add(column);
                }
            }
        }
        
        return result;
    }

    /**
     * Check if a race column represents Sprint Racing.
     */
    private boolean isSprintRacing(RaceColumn column) {
        String name = column.getName();
        return name != null && name.toLowerCase().contains("sprint");
    }
}
