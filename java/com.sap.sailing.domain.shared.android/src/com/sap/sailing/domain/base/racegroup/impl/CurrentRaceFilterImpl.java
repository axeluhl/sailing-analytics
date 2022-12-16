package com.sap.sailing.domain.base.racegroup.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.SeriesBase;
import com.sap.sailing.domain.base.racegroup.CurrentRaceFilter;
import com.sap.sailing.domain.base.racegroup.FilterableRace;
import com.sap.sailing.domain.base.racegroup.RaceCell;
import com.sap.sailing.domain.base.racegroup.RaceGroup;
import com.sap.sailing.domain.base.racegroup.RaceGroupSeries;
import com.sap.sailing.domain.base.racegroup.RaceGroupSeriesFleetRaceColumn;
import com.sap.sailing.domain.base.racegroup.RaceRow;
import com.sap.sailing.domain.base.racegroup.SeriesWithRows;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sse.common.Util;

/**
 * Determines a subset from a set of {@link FilterableRace} objects based on a definition of "being current." Current
 * races are interesting for a race officer to deal with. For example, the next set of races that are eligible for being
 * scheduled are part of this set; furthermore all running races. See {@link #getCurrentRaces()} for a more formal
 * definition what this filter considers to be a "current" race.
 * <p>
 * 
 * An instance of this type is initialized with the full set of races out of which the "current" subset shall be
 * extracted by the {@link #getCurrentRaces()} method. The full set of races is expected to skip no race columns and no
 * fleets and no series, or in other words, hold all races that form the regatta structure. This is important for the
 * "predecessor" relation which uses the fleet and series indices, such as
 * {@link FilterableRace#getZeroBasedIndexInFleet()} and {@link FilterableRace#getZeroBasedSeriesIndex()}.
 * 
 * @author Axel Uhl (d043530)
 *
 * @param <T>
 */
public class CurrentRaceFilterImpl<T extends FilterableRace> implements CurrentRaceFilter<T> {
    private final Iterable<T> allRaces;
    private final Map<RaceGroupSeriesFleetRaceColumn, T> racesByRaceGroupSeriesFleet;
    
    /**
     * Helpful in order to quickly find races that are in specific series
     */
    private final Map<RaceGroupSeries, Set<T>> racesByRaceGroupSeries;

    /**
     * Constructs a filter that can determine "current races" from a set of races representing all races of a set of
     * regattas / race groups. A copy of {@code allRaces} is constructed here, so later outside modifications to
     * {@code allRaces} will not have any effect on this filter. Clients need to construct a new instance if the set of
     * all races has changed.
     */
    public CurrentRaceFilterImpl(Iterable<T> allRaces) {
        final Collection<T> copyOfAllRaces = new HashSet<>();
        Util.addAll(allRaces, copyOfAllRaces);
        this.allRaces = copyOfAllRaces;
        this.racesByRaceGroupSeriesFleet = new HashMap<>();
        this.racesByRaceGroupSeries = new HashMap<>();
        for (final T race : allRaces) {
            racesByRaceGroupSeriesFleet.put(new RaceGroupSeriesFleetRaceColumn(race), race);
            final RaceGroupSeries key = new RaceGroupSeries(race);
            Set<T> racesForRaceGroupSeries = racesByRaceGroupSeries.get(key);
            if (racesForRaceGroupSeries == null) {
                racesForRaceGroupSeries = new HashSet<>();
                racesByRaceGroupSeries.put(key, racesForRaceGroupSeries);
            }
            racesForRaceGroupSeries.add(race);
        }
    }

    /**
     * A race is considered "current" and shall be shown if its status is between (inclusive)
     * {@link RaceLogRaceStatus#SCHEDULED} and {@link RaceLogRaceStatus#FINISHING}, or if the race has no predecessor in
     * the regatta / leaderboard and therefore could start the regatta, or if the race has an immediate predecessor (a
     * race A that must precede the race in question, say X, such that there is no other race B that also must precede X
     * with A having to precede B) which is at least in state {@link RaceLogRaceStatus#SCHEDULED}, or if the race is the
     * first in a series where for other fleets in the series a race is at least in state
     * {@link RaceLogRaceStatus#SCHEDULED} (the latter case is relevant if one or more race columns in the preceding
     * series were "skipped" and the next series has begun to be scheduled). However, a race with a status that is
     * before {@link RaceLogRaceStatus#SCHEDULED} is <em>not</em> considered current if a race exists in a subsequent
     * series that is at least {@link RaceLogRaceStatus#SCHEDULED}. Two typical cases for the latter exist: the regatta
     * structure may not have been adjusted properly regarding the number of races to be run in a qualification phase;
     * when the final series is scheduled, further unscheduled races of the qualification races are not to be shown
     * anymore. Similarly, when a qualification phase was not at all handled by the solution and only races from the
     * final and medal series are handled at all, unscheduled races from the qualification series shall not be
     * considered "current" when one or more races from the final or medal series have already been scheduled.
     */
    @Override
    public Set<T> getCurrentRaces() {
        final Set<T> filteredRaces = new HashSet<>();
        for (final T race : allRaces) {
            if (race.getStatus().getOrderNumber() <= RaceLogRaceStatus.FINISHING.getOrderNumber()) { // don't show finished races
                if (race.getStatus().getOrderNumber() >= RaceLogRaceStatus.SCHEDULED.getOrderNumber()) {
                    filteredRaces.add(race); // always show races at least scheduled and not yet finished
                } else {
                    assert race.getStatus().getOrderNumber() < RaceLogRaceStatus.SCHEDULED.getOrderNumber();
                    final boolean hasNoPredecessor=hasNoPredecessor(race);
                    if (!raceScheduledInLaterSeries(race) && // if a race is already at least scheduled in a later series,
                                                             // don't show unscheduled races in earlier series anymore
                        (hasNoPredecessor || hasImmediatePredecessorThatIsAtLeastScheduled(race) || isFirstInAlreadyScheduledSeries(race))) {
                        // show the race
                        filteredRaces.add(race);
                    }
                }
            }
        }
        return filteredRaces;
    }

    /**
     * Checks if {@code race} can be scheduled next in its series because it is in the first race column (for fleets
     * that may run in parallel in their series) or is additionally in the first fleet in the series (for fleets that
     * run their fleets sequentially; although this case is pretty bizarre because this race then should have been the
     * first to be scheduled; but a series would even count as started if someone scheduled a later race in that series)
     * and another race in the series is at least in state {@link RaceLogRaceStatus#SCHEDULED}. In this case, the
     * previous series is considered "terminated" by the scheduling of one or more races in the next series, and
     * therefore at least all "first" race in the series become eligible for being scheduled.
     */
    private boolean isFirstInAlreadyScheduledSeries(T race) {
        final boolean result;
        if (race.getZeroBasedIndexInFleet() == 0 && (race.getSeries().isFleetsCanRunInParallel() || Util.indexOf(race.getSeries().getFleets(), race.getFleet()) == 0)) {
            boolean foundRaceAtLeastScheduledInSameSeries = false;
            for (final T raceInSameSeries : racesByRaceGroupSeries.get(new RaceGroupSeries(race))) {
                if (raceInSameSeries.getStatus().getOrderNumber() >= RaceLogRaceStatus.SCHEDULED.getOrderNumber()) {
                    foundRaceAtLeastScheduledInSameSeries = true;
                    break;
                }
            }
            result = foundRaceAtLeastScheduledInSameSeries;
        } else {
            result = false;
        }
        return result;
    }

    private boolean raceScheduledInLaterSeries(T race) {
        for (final Entry<RaceGroupSeries, Set<T>> rgs : racesByRaceGroupSeries.entrySet()) {
            if (rgs.getKey().getRaceGroup() == race.getRaceGroup() && rgs.getKey().getSeriesOrder() > race.getZeroBasedSeriesIndex()) {
                for (final T r : rgs.getValue()) {
                    if (r.getStatus().getOrderNumber() >= RaceLogRaceStatus.SCHEDULED.getOrderNumber()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * For a race to have no predecessor in the regatta structure it has to be located in the first race column in the first series.
     * If the races of the different fleets in the series may {@link SeriesBase#isFleetsRunInParallel() run in parallel} then this is
     * already sufficient. If the races of the different fleets in the series are run in sequence, the race must additionally be
     * in the first fleet of the series.
     */
    private boolean hasNoPredecessor(FilterableRace race) {
        return race.getZeroBasedIndexInFleet() == 0 && isInFirstNonEmptySeries(race) &&
                (race.getSeries().isFleetsCanRunInParallel() || Util.indexOf(race.getSeries().getFleets(), race.getFleet()) == 0);
    }

    private boolean isInFirstNonEmptySeries(FilterableRace race) {
        for (int seriesIndex = race.getZeroBasedSeriesIndex() - 1; seriesIndex>=0; seriesIndex--) {
            final SeriesWithRows seriesWithRows = Util.get(race.getRaceGroup().getSeries(), seriesIndex);
            if (!isEmptySeries(seriesWithRows)) {
                return false;
            }
        }
        return true;
    }

    private boolean isEmptySeries(SeriesWithRows seriesWithRows) {
        final Iterable<RaceRow> raceRows = seriesWithRows.getRaceRows();
        return !raceRows.iterator().hasNext() || !raceRows.iterator().next().getCells().iterator().hasNext();
    }
    
    /**
     * Two cases need to be distinguished: the races in different fleets of the same series may be run
     * in parallel or not.<p>
     * 
     * Parallel fleets in series: the immediate predecessor is the race in the same fleet in the immediately preceding
     * race column; or if {@code race} is in the first column of its series, all races in the last column of the immediately
     * preceding series.<p>
     * 
     * Sequential fleets in series: the immediate predecessor is the race that immediately precedes {@code race} in
     * the list of races constructed by looping over all columns of the series and for each column looping over all
     * fleets. If {@code race} is the first in this sequence of its series, then the immediate predecessors is/are the
     * last race(s) (multiple if the previous series runs its fleets in parallel) of the immediately preceding series.<p>
     * 
     * Precondition: {@code !hasNoPredecessor(race)}
     * 
     * @return whether any of the predecessors (of which there is least one because of the precondition) have a
     * {@link RaceLogRaceStatus race status} whose {@link RaceLogRaceStatus#getOrderNumber() order number} is at least
     * {@link RaceLogRaceStatus#SCHEDULED SCHEDULED.getOrderNumber()}.
     */
    private boolean hasImmediatePredecessorThatIsAtLeastScheduled(FilterableRace race) {
        final Set<FilterableRace> immediatePredecessors = new HashSet<>();
        final RaceGroup raceGroup = race.getRaceGroup();
        if (race.getSeries().isFleetsCanRunInParallel()) {
            if (race.getZeroBasedIndexInFleet() == 0) {
                // it's in the first column; for fleets that may run in parallel, the immediate predecessors are
                // in the last column of the immediately preceding series:
                Util.addAll(getLastRacesFromImmediatelyPrecedingSeries(race, raceGroup), immediatePredecessors);
            } else {
                // not the first race column in its fleet; for parallel fleets, only the immediate predecessor
                // in the same fleet is an immediate predecessor for race
                RaceRow raceRow = Util.get(raceGroup.getSeries(), race.getZeroBasedSeriesIndex()).getRaceRow(race.getFleet());
                RaceCell previousCellInFleet = Util.get(raceRow.getCells(), race.getZeroBasedIndexInFleet()-1);
                immediatePredecessors.add(racesByRaceGroupSeriesFleet.get(
                        new RaceGroupSeriesFleetRaceColumn(raceGroup, race.getSeries(), race.getFleet(), previousCellInFleet.getName())));
            }
        } else {
            // race's series is run sequentially, within one race column one fleet after the other, in the order of the
            // fleets as specified by the series; the immediate predecessor is within this series the previous element in this
            // ordering, or the last race(s) from the immediately preceding series if race is in the first column's first fleet.
            final int zeroBasedIndexOfFleetInSeries = Util.indexOf(race.getSeries().getFleets(), race.getFleet());
            if (race.getZeroBasedIndexInFleet() == 0 && zeroBasedIndexOfFleetInSeries == 0) {
                Util.addAll(getLastRacesFromImmediatelyPrecedingSeries(race, raceGroup), immediatePredecessors);
            } else {
                final Fleet fleetOfImmediatelyPrecedingRace;
                final int zeroBasedIndexOfImmediatelyPrecedingRaceInFleet;
                if (zeroBasedIndexOfFleetInSeries == 0) {
                    // race is in first fleet; wrap around to last fleet in immediately preceding column
                    fleetOfImmediatelyPrecedingRace = Util.last(race.getSeries().getFleets());
                    zeroBasedIndexOfImmediatelyPrecedingRaceInFleet = race.getZeroBasedIndexInFleet() - 1;
                } else {
                    fleetOfImmediatelyPrecedingRace = Util.get(race.getSeries().getFleets(), zeroBasedIndexOfFleetInSeries-1);
                    zeroBasedIndexOfImmediatelyPrecedingRaceInFleet = race.getZeroBasedIndexInFleet();
                }
                final RaceGroupSeriesFleetRaceColumn identifierOfImmediatelyPrecedingRace =
                        new RaceGroupSeriesFleetRaceColumn(raceGroup, race.getSeries(), fleetOfImmediatelyPrecedingRace,
                        Util.get(Util.get(raceGroup.getSeries(), race.getZeroBasedSeriesIndex()).getRaceRow(
                                fleetOfImmediatelyPrecedingRace).getCells(), zeroBasedIndexOfImmediatelyPrecedingRaceInFleet).getName());
                immediatePredecessors.add(racesByRaceGroupSeriesFleet.get(identifierOfImmediatelyPrecedingRace));
            }
        }
        final Iterator<FilterableRace> i = immediatePredecessors.iterator();
        boolean result = false;
        while (!result && i.hasNext()) {
            final RaceLogRaceStatus nextStatus = i.next().getStatus();
            result = nextStatus.getOrderNumber() >= RaceLogRaceStatus.SCHEDULED.getOrderNumber();
        }
        return result;
    }

    private Iterable<T> getLastRacesFromImmediatelyPrecedingSeries(FilterableRace race, final RaceGroup raceGroup) {
        final Set<T> lastRacesInImmediatelyPrecedingSeries = new HashSet<>();
        final List<SeriesWithRows> immediatelyPrecedingSerieses = Util.asList(raceGroup.getSeries()).subList(0, race.getZeroBasedSeriesIndex());
        SeriesWithRows lastPrecedingNonEmptySeries = Util.last(Util.filter(immediatelyPrecedingSerieses, series->!isEmptySeries(series)));
        if (lastPrecedingNonEmptySeries.isFleetsCanRunInParallel()) {
            // add all last races of all fleets in immediately preceding series because they may be run in parallel
            for (final RaceRow rowInImmediatelyPrecedingSeries : lastPrecedingNonEmptySeries.getRaceRows()) {
                final T lastRaceInRow = getLastRaceInRow(raceGroup, lastPrecedingNonEmptySeries, rowInImmediatelyPrecedingSeries);
                lastRacesInImmediatelyPrecedingSeries.add(lastRaceInRow);
            }
        } else {
            // add only the last race of the last fleet in the immediately preceding series
            final RaceRow lastRowInImmediatelyPrecedingSeries = Util.last(lastPrecedingNonEmptySeries.getRaceRows());
            lastRacesInImmediatelyPrecedingSeries.add(getLastRaceInRow(raceGroup, lastPrecedingNonEmptySeries, lastRowInImmediatelyPrecedingSeries));
        }
        return lastRacesInImmediatelyPrecedingSeries;
    }

    private T getLastRaceInRow(final RaceGroup raceGroup, final SeriesWithRows immediatelyPrecedingSeries,
            final RaceRow rowInImmediatelyPrecedingSeries) {
        final RaceCell lastCell = Util.last(rowInImmediatelyPrecedingSeries.getCells());
        RaceGroupSeriesFleetRaceColumn identifierOfALastRaceInImmediatelyPrecedingSeries =
                new RaceGroupSeriesFleetRaceColumn(raceGroup, immediatelyPrecedingSeries,
                        rowInImmediatelyPrecedingSeries.getFleet(), lastCell.getName());
        final T lastRaceInRow = racesByRaceGroupSeriesFleet.get(identifierOfALastRaceInImmediatelyPrecedingSeries);
        return lastRaceInRow;
    }
}
