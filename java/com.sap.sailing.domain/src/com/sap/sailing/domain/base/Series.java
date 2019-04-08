package com.sap.sailing.domain.base;

import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;

/**
 * A series is a part of a {@link Regatta}. Series are ordered within the regatta, and rules for who is assigned to
 * which series may exist on the regatta. For example, a regatta may have a qualification series, a final series, and a
 * medal "series" with usually only a single medal race. Each series has one or more fleets, deciding how many races per
 * race column have to be run in this round. For example, if the 49er regatta has so many competitors that they cannot
 * all start in one race, the qualification series can be split into two {@link Fleet}s, "Yellow" and "Blue," each
 * getting their separate races. Fleet assignment may or may not vary. This usually depends on the series'
 * characteristics of having ordered or unordered fleets.<p>
 * 
 * A series may define its result discarding scheme. If it does, a regatta leaderboard for the containing regatta
 * has to adhere to this and may not define its own cross-cutting result discarding scheme. If one or more series
 * in the regatta choose to define their own result discarding scheme, discards are determined per series and not
 * per leaderboard.<p>
 * 
 * To receive notifications when {@link TrackedRace tracked races} are linked to or unlinked from any of this series'
 * columns, {@link RaceColumnListener}s can be added / removed.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public interface Series extends SeriesBase {
    
    static public final String DEFAULT_NAME = "Default";
    
    /**
     * A series consists of one or more "race columns." Some people would just say "race," but we use the term "race" for
     * something that has a single start time and start line; so if each fleet in a series gets their own start for
     * something called "R2", those are as many "races" as we have fleets; therefore, we use "race column" instead to
     * describe all "races" named, e.g., "R3" in a series.
     */
    Iterable<? extends RaceColumnInSeries> getRaceColumns();
    
    RaceColumnInSeries getRaceColumnByName(String columnName);
    
    void setIsMedal(boolean isMedal);

    void setIsFleetsCanRunInParallel(boolean isFleetsCanRunInParallel);

    Fleet getFleetByName(String fleetName);

    /**
     * @param trackedRegattaRegistry
     *            used to find the {@link TrackedRegatta} for this column's series' {@link Series#getRegatta() regatta}
     *            in order to re-associate a {@link TrackedRace} passed to {@link #setTrackedRace(Fleet, TrackedRace)}
     *            with this column's series' {@link TrackedRegatta}, and the tracked race's {@link RaceDefinition} with
     *            this column's series {@link Regatta}, respectively. If <code>null</code>, the re-association won't be
     *            carried out.
     */
    RaceColumnInSeries addRaceColumn(String raceColumnName, TrackedRegattaRegistry trackedRegattaRegistry);
    
    RaceColumnInSeries addRaceColumn(int insertIndex, String raceColumnName, TrackedRegattaRegistry trackedRegattaRegistry);

    void moveRaceColumnUp(String raceColumnName);
    
    void moveRaceColumnDown(String raceColumnName);
    
    void removeRaceColumn(String raceColumnName);
    
    /**
     * If not <code>null</code>, a containing regatta's leaderboard must obey this rule and in particular cannot define
     * a "cross-cutting" result discarding rule where discards may be arbitrarily distributed across series.
     */
    ThresholdBasedResultDiscardingRule getResultDiscardingRule();
    
    void setResultDiscardingRule(ThresholdBasedResultDiscardingRule resultDiscardingRule);
    
    /**
     * If not {@code null}, defines an upper inclusive limit for the number of races that may be discarded from
     * this series. For example, when setting this to {@code 1} for a final series in a regatta that has a
     * qualification and a final series, when the second discard becomes available and the series don't define
     * their own discarding rules, two discards may be picked from the qualification series, but at most one
     * could be selected in the final even if another final race has a score worse than that of all
     * qualification races.
     */
    Integer getMaximumNumberOfDiscards();
    
    void setMaximumNumberOfDiscards(Integer maximumNumberOfDiscards);

    Regatta getRegatta();
    
    /**
     * Sets this series' regatta.
     */
    void setRegatta(Regatta regatta);
    
    void addRaceColumnListener(RaceColumnListener listener);
    
    void removeRaceColumnListener(RaceColumnListener listener);

    /**
     * @return whether this series defines its local result discarding rule; if so, any leaderboard based on the
     *         enclosing regatta has to respect this and has to use a result discarding rule implementation that keeps
     *         discards local to each series rather than spreading them across the entire leaderboard.
     */
    boolean definesSeriesDiscardThresholds();
    
    /**
     * By default, a competitor's total score is computed by summing up the non-discarded total points of each race
     * across the leaderboard, considering the {@link RaceColumn#getFactor() column factors}. Some series, however, are
     * defined such that participating competitors start the series with a zero score. Any carry-forward would then have
     * to be modeled as a first carry-forward "race" in the series which the ISAF in 2013 has defined as being a race
     * logically, therefore also being discardable. If this method returns <code>true</code>, this series advises the
     * leaderboard and scoring scheme to start counting the total points at this series with zero.
     * <p>
     * 
     * This condition propagates to the first race column of the series which is then used by the leaderboard and
     * scoring scheme.
     */
    boolean isStartsWithZeroScore();
    
    void setStartsWithZeroScore(boolean startsWithZeroScore);

    boolean isFirstColumnIsNonDiscardableCarryForward();

    void setFirstColumnIsNonDiscardableCarryForward(boolean firstColumnIsNonDiscardableCarryForward);

    /**
     * When a series has more than one fleet, there are two different options for scoring it. Either the scoring scheme is applied
     * to the sequence of competitors one gets when first ordering the competitors by fleets and then within each fleet by their
     * rank in the fleet's race; or the scoring scheme is applied to each fleet separately, leading to the best score being awarded
     * in the column as many times as there are fleets in the column. For the former case, this method returns <code>true</code>.
     */
    boolean hasSplitFleetContiguousScoring();

    void setSplitFleetContiguousScoring(boolean hasSplitFleetScore);

}
