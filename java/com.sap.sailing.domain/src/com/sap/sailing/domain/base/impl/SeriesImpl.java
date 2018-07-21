package com.sap.sailing.domain.base.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnInSeries;
import com.sap.sailing.domain.base.RaceColumnListener;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.leaderboard.ResultDiscardingRule;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;
import com.sap.sailing.util.impl.RaceColumnListeners;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.RenamableImpl;

/**
 * A series listens on its columns; however, a veto for column addition isn't done here but in a {@link RegattaLeaderboard}.
 * 
 * @see #addRaceColumn(String, TrackedRegattaRegistry)
 */
public class SeriesImpl extends RenamableImpl implements Series, RaceColumnListener {
    private static final long serialVersionUID = -1640404303144907381L;
    private final Map<String, Fleet> fleetsByName;
    private final List<Fleet> fleetsInAscendingOrder;
    private final List<RaceColumnInSeries> raceColumns;
    private boolean isMedal;
    private boolean isFleetsCanRunInParallel;
    private Regatta regatta;
    private final RaceColumnListeners raceColumnListeners;
    private ThresholdBasedResultDiscardingRule resultDiscardingRule;

    /**
     * If not {@code null}, defines an upper inclusive limit for the number of races that may be discarded from
     * this series. For example, when setting this to {@code 1} for a final series in a regatta that has a
     * qualification and a final series, when the second discard becomes available and the series don't define
     * their own discarding rules, two discards may be picked from the qualification series, but at most one
     * could be selected in the final even if another final race has a score worse than that of all
     * qualification races.
     */
    private Integer maximumNumberOfDiscards;
    
    /**
     * If set, the series doesn't take over the scores from any previous series but starts with zero scores for all its
     * competitors
     */
    private boolean startsWithZeroScore;
    
    /**
     * If set, the first race column is not discardable. This is usually very helpful if the series starts with a
     * carry-forward score from a previous series.
     */
    private boolean firstColumnIsNonDiscardableCarryForward;
    
    /**
     * When a column has more than one fleet, there are two different options for scoring it. Either the scoring scheme is applied
     * to the sequence of competitors one gets when first ordering the competitors by fleets and then within each fleet by their
     * rank in the fleet's race; or the scoring scheme is applied to each fleet separately, leading to the best score being awarded
     * in the column as many times as there are fleets in the column. For the latter case, this field is <code>false</code> which is
     * also the default.
     */
    private boolean hasSplitFleetContiguousScoring;
    
    /**
     * @param fleets
     *            must be non-empty
     * @param trackedRegattaRegistry
     *            used to find the {@link TrackedRegatta} for this column's series' {@link Series#getRegatta() regatta}
     *            in order to re-associate a {@link TrackedRace} passed to {@link #setTrackedRace(Fleet, TrackedRace)}
     *            with this column's series' {@link TrackedRegatta}, and the tracked race's {@link RaceDefinition} with
     *            this column's series {@link Regatta}, respectively. If <code>null</code>, the re-association won't be
     *            carried out.
     */
    public SeriesImpl(String name, boolean isMedal, boolean isFleetsCanRunInParallel, Iterable<? extends Fleet> fleets, Iterable<String> raceColumnNames,
            TrackedRegattaRegistry trackedRegattaRegistry) {
        super(name);
        if (fleets == null || Util.isEmpty(fleets)) {
            throw new IllegalArgumentException("Series must have at least one fleet");
        }
        this.fleetsByName = new HashMap<String, Fleet>();
        for (Fleet fleet : fleets) {
            this.fleetsByName.put(fleet.getName(), fleet);
        }
        fleetsInAscendingOrder = new ArrayList<Fleet>();
        Util.addAll(fleets, fleetsInAscendingOrder);
        Collections.sort(fleetsInAscendingOrder);
        this.raceColumns = new ArrayList<RaceColumnInSeries>();
        this.isMedal = isMedal;
        this.isFleetsCanRunInParallel = isFleetsCanRunInParallel; 
        this.raceColumnListeners = new RaceColumnListeners();
        for (String raceColumnName : raceColumnNames) {
            addRaceColumn(raceColumnName, trackedRegattaRegistry);
        }
    }

    @Override
    public void addRaceColumnListener(RaceColumnListener listener) {
        raceColumnListeners.addRaceColumnListener(listener);
    }

    @Override
    public void removeRaceColumnListener(RaceColumnListener listener) {
        raceColumnListeners.removeRaceColumnListener(listener);
    }
    
    @Override
    public Regatta getRegatta() {
        return regatta;
    }

    @Override
    public void setRegatta(Regatta regatta) {
        if (this.regatta != null) {
            detachRaceExecutionOrderProviderFromTrackedRacesInRaceColumns();
        }
        this.regatta = regatta;
        if (this.regatta != null) {
            attachRaceExecutionOrderProviderToTrackedRacesInRaceColumns();
        } else {
            detachRaceExecutionOrderProviderFromTrackedRacesInRaceColumns();
        }
    }

    public Iterable<? extends Fleet> getFleets() {
        return fleetsInAscendingOrder;
    }

    @Override
    public Fleet getFleetByName(String fleetName) {
        return fleetsByName.get(fleetName);
    }

    @Override
    public Iterable<? extends RaceColumnInSeries> getRaceColumns() {
        return raceColumns;
    }

    /**
     * @param trackedRegattaRegistry
     *            used to find the {@link TrackedRegatta} for this column's series' {@link Series#getRegatta() regatta}
     *            in order to re-associate a {@link TrackedRace} passed to {@link #setTrackedRace(Fleet, TrackedRace)}
     *            with this column's series' {@link TrackedRegatta}, and the tracked race's {@link RaceDefinition} with
     *            this column's series {@link Regatta}, respectively. If <code>null</code>, the re-association won't be
     *            carried out.
     */
    @Override
    public RaceColumnInSeries addRaceColumn(String raceColumnName, TrackedRegattaRegistry trackedRegattaRegistry) {
        return addRaceColumn(raceColumns.size(), raceColumnName, trackedRegattaRegistry);
    }
    
    @Override
    public RaceColumnInSeries addRaceColumn(int insertIndex, String raceColumnName, TrackedRegattaRegistry trackedRegattaRegistry) {
        RaceColumnInSeriesImpl result = createRaceColumn(raceColumnName, trackedRegattaRegistry);
        if (raceColumnListeners.canAddRaceColumnToContainer(result)) {
            result.addRaceColumnListener(this);
            raceColumns.add(insertIndex, result);
            raceColumnListeners.notifyListenersAboutRaceColumnAddedToContainer(result);
        } else {
            result = null;
        }
        return result;
    }

    /**
     * @param trackedRegattaRegistry
     *            used to find the {@link TrackedRegatta} for this column's series' {@link Series#getRegatta() regatta}
     *            in order to re-associate a {@link TrackedRace} passed to {@link #setTrackedRace(Fleet, TrackedRace)}
     *            with this column's series' {@link TrackedRegatta}, and the tracked race's {@link RaceDefinition} with
     *            this column's series {@link Regatta}, respectively. If <code>null</code>, the re-association won't be
     *            carried out.
     */
    private RaceColumnInSeriesImpl createRaceColumn(String raceColumnName, TrackedRegattaRegistry trackedRegattaRegistry) {
        return new RaceColumnInSeriesImpl(
                raceColumnName, 
                this, 
                trackedRegattaRegistry);
    }
    
    private void attachRaceExecutionOrderProviderToTrackedRacesInRaceColumns() {
        for (RaceColumnInSeries raceColumnInSeries : raceColumns) {
            for (Fleet fleet : raceColumnInSeries.getFleets()) {
                TrackedRace trackedRace = raceColumnInSeries.getTrackedRace(fleet);
                if (trackedRace != null && regatta != null) {
                    trackedRace.attachRaceExecutionProvider(regatta.getRaceExecutionOrderProvider());
                }
            }
        }
    }
    
    private void detachRaceExecutionOrderProviderFromTrackedRacesInRaceColumns() {
        for (RaceColumnInSeries raceColumnInSeries : raceColumns) {
            for (Fleet fleet : raceColumnInSeries.getFleets()) {
                TrackedRace trackedRace = raceColumnInSeries.getTrackedRace(fleet);
                if (trackedRace != null && regatta != null && regatta.getRaceExecutionOrderProvider() != null) {
                    trackedRace.detachRaceExecutionOrderProvider(regatta.getRaceExecutionOrderProvider());
                }
            }
        }
    }

    @Override
    public void moveRaceColumnUp(String raceColumnName) {
        boolean didFirstColumnChange = false; // if it changes and the series starts scoring with zero, notify the change
        // start at second element because first can't be moved up
        for (int i=1; i<raceColumns.size(); i++) {
            RaceColumnInSeries rc = raceColumns.get(i);
            if (rc.getName().equals(raceColumnName)) {
                raceColumns.remove(i);
                if (i==1) {
                    didFirstColumnChange = true;
                }
                raceColumnListeners.notifyListenersAboutRaceColumnRemovedFromContainer(rc);
                raceColumns.add(i-1, rc);
                raceColumnListeners.notifyListenersAboutRaceColumnAddedToContainer(rc);
                break;
            }
        }
        if (didFirstColumnChange && startsWithZeroScore) {
            notifyIsStartsWithZeroScoreChangedForFirstTwoColumns();
        }
    }

    private void notifyIsStartsWithZeroScoreChangedForFirstTwoColumns() {
        final RaceColumnInSeries first = raceColumns.get(0);
        raceColumnListeners.notifyListenersAboutIsStartsWithZeroScoreChanged(first, first.isStartsWithZeroScore());
        final RaceColumnInSeries second = raceColumns.get(1);
        raceColumnListeners.notifyListenersAboutIsStartsWithZeroScoreChanged(second, second.isStartsWithZeroScore());
    }

    @Override
    public void moveRaceColumnDown(String raceColumnName) {
        boolean didFirstColumnChange = false; // if it changes and the series starts scoring with zero, notify the change
        // end at second-last element because last can't be moved down
        for (int i=0; i<raceColumns.size()-1; i++) {
            RaceColumnInSeries rc = raceColumns.get(i);
            if (rc.getName().equals(raceColumnName)) {
                raceColumns.remove(i);
                if (i==0) {
                    didFirstColumnChange = true;
                }
                raceColumnListeners.notifyListenersAboutRaceColumnRemovedFromContainer(rc);
                raceColumns.add(i+1, rc);
                raceColumnListeners.notifyListenersAboutRaceColumnAddedToContainer(rc);
                break;
            }
        }
        if (didFirstColumnChange && startsWithZeroScore) {
            notifyIsStartsWithZeroScoreChangedForFirstTwoColumns();
        }
    }

    @Override
    public void removeRaceColumn(String raceColumnName) {
        RaceColumnInSeries rc = getRaceColumnByName(raceColumnName);
        if (rc != null) {
            raceColumns.remove(rc);
            raceColumnListeners.notifyListenersAboutRaceColumnRemovedFromContainer(rc);
            rc.removeRaceColumnListener(this);
        }
    }

    @Override
    public RaceColumnInSeries getRaceColumnByName(String columnName) {
        for (RaceColumnInSeries raceColumn : getRaceColumns()) {
            if (raceColumn.getName().equals(columnName)) {
                return raceColumn;
            }
        }
        return null;
    }

    @Override
    public boolean isMedal() {
        return isMedal;
    }

    @Override
    public void setIsMedal(boolean isMedal) {
        boolean oldIsMedal = this.isMedal;
        this.isMedal = isMedal;
        if (oldIsMedal != isMedal) {
            for (RaceColumn raceColumn : getRaceColumns()) {
                raceColumnListeners.notifyListenersAboutIsMedalRaceChanged(raceColumn, isMedal);
            }
        }
    }

    @Override
    public boolean isFleetsCanRunInParallel() {
        return isFleetsCanRunInParallel;
    }

    @Override
    public void setIsFleetsCanRunInParallel(boolean isFleetsCanRunInParallel) {
        boolean oldIsFleetsCanRunInParallel = this.isFleetsCanRunInParallel;
        this.isFleetsCanRunInParallel = isFleetsCanRunInParallel;
        if (oldIsFleetsCanRunInParallel != isFleetsCanRunInParallel) {
            for (RaceColumn raceColumn : getRaceColumns()) {
                raceColumnListeners.notifyListenersAboutIsFleetsCanRunInParallelChanged(raceColumn, isFleetsCanRunInParallel);
            }
        }
    }

    @Override
    public void trackedRaceLinked(RaceColumn raceColumn, Fleet fleet, TrackedRace trackedRace) {
        raceColumnListeners.notifyListenersAboutTrackedRaceLinked(raceColumn, fleet, trackedRace);
    }

    @Override
    public void trackedRaceUnlinked(RaceColumn raceColumn, Fleet fleet, TrackedRace trackedRace) {
        raceColumnListeners.notifyListenersAboutTrackedRaceUnlinked(raceColumn, fleet, trackedRace);
    }

    @Override
    public void isMedalRaceChanged(RaceColumn raceColumn, boolean newIsMedalRace) {
        raceColumnListeners.notifyListenersAboutIsMedalRaceChanged(raceColumn, newIsMedalRace);
    }

    @Override
    public void isFleetsCanRunInParallelChanged(RaceColumn raceColumn, boolean newIsFleetsCanRunInParallel) {
        raceColumnListeners.notifyListenersAboutIsFleetsCanRunInParallelChanged(raceColumn, newIsFleetsCanRunInParallel);
    }
    @Override
    public void isStartsWithZeroScoreChanged(RaceColumn raceColumn, boolean newIsStartsWithZeroScore) {
        raceColumnListeners.notifyListenersAboutIsStartsWithZeroScoreChanged(raceColumn, newIsStartsWithZeroScore);
    }

    @Override
    public void hasSplitFleetContiguousScoringChanged(RaceColumn raceColumn, boolean hasSplitFleetContiguousScoring) {
        raceColumnListeners.notifyListenersAboutHasSplitFleetContiguousScoringChanged(raceColumn, hasSplitFleetContiguousScoring);
    }

    @Override
    public void isFirstColumnIsNonDiscardableCarryForwardChanged(RaceColumn raceColumn, boolean firstColumnIsNonDiscardableCarryForward) {
        raceColumnListeners.notifyListenersAboutIsFirstColumnIsNonDiscardableCarryForwardChanged(raceColumn, firstColumnIsNonDiscardableCarryForward);
    }

    @Override
    public void raceColumnAddedToContainer(RaceColumn raceColumn) {
        raceColumnListeners.notifyListenersAboutRaceColumnAddedToContainer(raceColumn);
    }

    @Override
    public void raceColumnRemovedFromContainer(RaceColumn raceColumn) {
        raceColumnListeners.notifyListenersAboutRaceColumnRemovedFromContainer(raceColumn);
    }

    @Override
    public void raceColumnMoved(RaceColumn raceColumn, int newIndex) {
        raceColumnListeners.notifyListenersAboutRaceColumnMoved(raceColumn, newIndex);
    }

    @Override
    public void raceColumnNameChanged(RaceColumn raceColumn, String oldName, String newName) {
        raceColumnListeners.notifyListenersAboutRaceColumnNameChanged(raceColumn, oldName, newName);
    }

    @Override
    public void factorChanged(RaceColumn raceColumn, Double oldFactor, Double newFactor) {
        raceColumnListeners.notifyListenersAboutFactorChanged(raceColumn, oldFactor, newFactor);
    }

    @Override
    public void competitorDisplayNameChanged(Competitor competitor, String oldDisplayName, String displayName) {
        raceColumnListeners.notifyListenersAboutCompetitorDisplayNameChanged(competitor, oldDisplayName, displayName);
    }

    @Override
    public void resultDiscardingRuleChanged(ResultDiscardingRule oldDiscardingRule, ResultDiscardingRule newDiscardingRule) {
        raceColumnListeners.notifyListenersAboutResultDiscardingRuleChanged(oldDiscardingRule, newDiscardingRule);
    }

    @Override
    public void raceLogEventAdded(RaceColumn raceColumn, RaceLogIdentifier raceLogIdentifier, RaceLogEvent event) {
        raceColumnListeners.notifyListenersAboutRaceLogEventAdded(raceColumn, raceLogIdentifier, event);
    }

    @Override
    public boolean isTransient() {
        return false;
    }

    @Override
    public ThresholdBasedResultDiscardingRule getResultDiscardingRule() {
        return resultDiscardingRule;
    }

    @Override
    public void setResultDiscardingRule(ThresholdBasedResultDiscardingRule resultDiscardingRule) {
        ThresholdBasedResultDiscardingRule oldResultDiscardingRule = this.resultDiscardingRule;
        if (!Util.equalsWithNull(oldResultDiscardingRule, resultDiscardingRule)) {
            this.resultDiscardingRule = resultDiscardingRule;
            raceColumnListeners.notifyListenersAboutResultDiscardingRuleChanged(oldResultDiscardingRule, resultDiscardingRule);
        }
        this.resultDiscardingRule = resultDiscardingRule;
    }

    @Override
    public Integer getMaximumNumberOfDiscards() {
        return maximumNumberOfDiscards;
    }

    @Override
    public void setMaximumNumberOfDiscards(Integer maximumNumberOfDiscards) {
        this.maximumNumberOfDiscards = maximumNumberOfDiscards;
    }

    @Override
    public boolean definesSeriesDiscardThresholds() {
        return getResultDiscardingRule() != null;
    }

    @Override
    public boolean isStartsWithZeroScore() {
        return startsWithZeroScore;
    }

    /**
     * @return <code>null</code> if the series doesn't currently have any columns
     */
    private RaceColumn getFirstRaceColumn() {
        RaceColumn result = null;
        if (!raceColumns.isEmpty()) {
            result = raceColumns.get(0);
        }
        return result;
    }
    
    @Override
    public void setStartsWithZeroScore(boolean startsWithZeroScore) {
        boolean oldStartsWithZeroScore = this.startsWithZeroScore;
        if (oldStartsWithZeroScore != startsWithZeroScore) {
            this.startsWithZeroScore = startsWithZeroScore;
            RaceColumn firstRaceColumnInSeries = getFirstRaceColumn();
            if (firstRaceColumnInSeries != null) {
                raceColumnListeners.notifyListenersAboutIsStartsWithZeroScoreChanged(firstRaceColumnInSeries, startsWithZeroScore);
            }
        }
    }

    @Override
    public void setSplitFleetContiguousScoring(boolean hasSplitFleetContiguousScoring) {
        if (hasSplitFleetContiguousScoring != this.hasSplitFleetContiguousScoring) {
            this.hasSplitFleetContiguousScoring = hasSplitFleetContiguousScoring;
            for (RaceColumn raceColumn : getRaceColumns()) {
                raceColumnListeners.notifyListenersAboutHasSplitFleetContiguousScoringChanged(raceColumn, hasSplitFleetContiguousScoring);
            }
        }
    }

    @Override
    public boolean isFirstColumnIsNonDiscardableCarryForward() {
        return firstColumnIsNonDiscardableCarryForward;
    }

    @Override
    public void setFirstColumnIsNonDiscardableCarryForward(boolean firstColumnIsNonDiscardableCarryForward) {
        boolean oldFirstColumnIsNonDiscardableCarryForward = this.firstColumnIsNonDiscardableCarryForward;
        if (oldFirstColumnIsNonDiscardableCarryForward != firstColumnIsNonDiscardableCarryForward) {
            this.firstColumnIsNonDiscardableCarryForward = firstColumnIsNonDiscardableCarryForward;
            RaceColumn firstRaceColumnInSeries = getFirstRaceColumn();
            if (firstRaceColumnInSeries != null) {
                raceColumnListeners.notifyListenersAboutIsFirstColumnIsNonDiscardableCarryForwardChanged(firstRaceColumnInSeries, firstColumnIsNonDiscardableCarryForward);
            }
        }
        this.firstColumnIsNonDiscardableCarryForward = firstColumnIsNonDiscardableCarryForward;
    }

    @Override
    public boolean hasSplitFleetContiguousScoring() {
        return hasSplitFleetContiguousScoring;
    }
}
