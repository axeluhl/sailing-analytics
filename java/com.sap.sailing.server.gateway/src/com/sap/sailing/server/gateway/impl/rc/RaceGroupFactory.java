package com.sap.sailing.server.gateway.impl.rc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnInSeries;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.configuration.RegattaConfiguration;
import com.sap.sailing.domain.base.impl.SeriesImpl;
import com.sap.sailing.domain.base.racegroup.RaceCell;
import com.sap.sailing.domain.base.racegroup.RaceGroup;
import com.sap.sailing.domain.base.racegroup.RaceRow;
import com.sap.sailing.domain.base.racegroup.SeriesWithRows;
import com.sap.sailing.domain.base.racegroup.impl.RaceCellImpl;
import com.sap.sailing.domain.base.racegroup.impl.RaceGroupImpl;
import com.sap.sailing.domain.base.racegroup.impl.RaceRowImpl;
import com.sap.sailing.domain.base.racegroup.impl.SeriesWithRowsImpl;
import com.sap.sailing.domain.common.LeaderboardNameConstants;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.TargetTimeInfo;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.polars.NotEnoughDataHasBeenAddedException;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * Used to convert {@link Leaderboard}s to {@link RaceGroup}s.
 */
public class RaceGroupFactory {
    private static final Logger logger = Logger.getLogger(RaceGroupFactory.class.getName());

    /**
     * Convert given {@link RegattaLeaderboard} to a {@link RaceGroup}.
     * @param leaderboard to be converted.
     * @return the {@link RaceGroup}.
     */
    public RaceGroup convert(RegattaLeaderboard leaderboard) {
        String name = leaderboard.getName();
        CourseArea courseArea = leaderboard.getDefaultCourseArea();
        Regatta regatta = ((RegattaLeaderboard) leaderboard).getRegatta();
        Iterable<SeriesWithRows> series = getSeries(leaderboard);
        return new RaceGroupImpl(name, leaderboard.getDisplayName(), regatta.getBoatClass(), regatta.canBoatsOfCompetitorsChangePerRace(), 
                courseArea, series, regatta.getRegattaConfiguration());
    }

    /**
     * Convert given {@link FlexibleLeaderboard} to a {@link RaceGroup}.
     * @param leaderboard to be converted.
     * @return the {@link RaceGroup}.
     */
    public RaceGroup convert(FlexibleLeaderboard leaderboard) {
        String name = leaderboard.getName();
        CourseArea courseArea = leaderboard.getDefaultCourseArea();
        BoatClass boatClass = null;
        RegattaConfiguration configuration = null;
        Iterable<SeriesWithRows> series = getSeries(leaderboard);
        return new RaceGroupImpl(name, leaderboard.getDisplayName(), boatClass, false, courseArea, series, configuration);
    }

    private Iterable<SeriesWithRows> getSeries(Leaderboard leaderboard) {
        Map<Series, List<RaceColumn>> seriesToRaceColumns = getSeriesToRaceColumns(leaderboard);
        Collection<SeriesWithRows> seriesWithRows = new ArrayList<>();
        for (Series series : getSeriesIterable(leaderboard, seriesToRaceColumns)) {
            seriesWithRows
                    .add(new SeriesWithRowsImpl(series.getName(), series.isMedal(), series.isFleetsCanRunInParallel(),
                            getRows(series, seriesToRaceColumns.get(series), leaderboard.getScoringScheme())));
        }
        return seriesWithRows;
    }

    private Iterable<? extends Series> getSeriesIterable(Leaderboard leaderboard, Map<Series, List<RaceColumn>> seriesToRaceColumns) {
        // for RegattaLeaderboards we want to preserve the order of series
        if (leaderboard instanceof RegattaLeaderboard) {
            return ((RegattaLeaderboard) leaderboard).getRegatta().getSeries();
        }
        return seriesToRaceColumns.keySet();
    }

    /**
     * @param raceColumns
     *            must be provided in the same order in which they appear in the series / leaderboard
     */
    private Collection<RaceRow> getRows(Series series, List<RaceColumn> raceColumns, ScoringScheme scoringScheme) {
        Collection<RaceRow> rows = new ArrayList<>();
        for (Fleet fleet : series.getFleets()) {
            // We are taking the fleet name because there might be several "default fleet"
            // objects when TrackedRaces are linked onto this Leaderboard
            rows.add(new RaceRowImpl(fleet,
                    getCells(fleet.getName(), raceColumns, isFirstRaceColumnVirtual(series), scoringScheme)));
        }
        return rows;
    }
    
    private boolean isFirstRaceColumnVirtual(Series series){
        return series.isFirstColumnIsNonDiscardableCarryForward();
    }

    /**
     * Delivers the race cell in the order of the {@link RaceColumn}s provided in {@code raceColumns}. The
     * {@link RaceCell#getTargetTime() target times} are based on an
     * {@link TrackedRace#getEstimatedTimeToComplete(TimePoint) estimation} for the current point in time, when the
     * request is being made, using this time point to determine the wind speed and direction for each leg.
     */
    private List<RaceCell> getCells(String fleetName, List<RaceColumn> raceColumns, boolean isFirstRaceColumnVirtual, ScoringScheme scoringScheme) {
        boolean skippedFirst = false;
        List<RaceCell> cells = new ArrayList<>();
        if (raceColumns != null) {
            int zeroBasedIndexOfRaceInFleet = 0;
            final TimePoint now = MillisecondsTimePoint.now();
            for (final RaceColumn raceColumn : raceColumns) {
                if (isFirstRaceColumnVirtual && !skippedFirst) {
                    skippedFirst = true;
                } else {
                    final Fleet fleet = raceColumn.getFleetByName(fleetName);
                    final TrackedRace trackedRace = raceColumn.getTrackedRace(fleet);
                    TargetTimeInfo targetTime;
                    try {
                        targetTime = trackedRace == null ? null : trackedRace.getEstimatedTimeToComplete(now);
                    } catch (NotEnoughDataHasBeenAddedException | NoWindException e) {
                        logger.fine(()->"Unable to determine target time for "+trackedRace);
                        targetTime = null;
                    }
                    cells.add(new RaceCellImpl(raceColumn.getName(), raceColumn.getRaceLog(fleet),
                            scoringScheme.getScoreFactor(raceColumn), raceColumn.getExplicitFactor(),
                            zeroBasedIndexOfRaceInFleet++, targetTime));
                }
            }
        }
        return cells;
    }

    /**
     * Returns a series to race column mapping. If there are no series all race columns will
     * be mapped from a default series. The race column lists that appear as values in the result
     * maintain the order in which the race column objects appear in the original {@link Series}
     * or leaderboard.
     */
    private Map<Series, List<RaceColumn>> getSeriesToRaceColumns(Leaderboard leaderboard) {
        Map<Series, List<RaceColumn>> seriesToRaceColumns = new HashMap<>();

        Series defaultSeries = null;
        for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
            if (raceColumn instanceof RaceColumnInSeries) {
                Series raceColumnSeries = ((RaceColumnInSeries) raceColumn).getSeries();
                insertSeriesIfNew(seriesToRaceColumns, raceColumnSeries).add(raceColumn);
            } else {
                if (defaultSeries == null) {
                    defaultSeries = createDefaultSeries(raceColumn.getFleets());
                    insertSeriesIfNew(seriesToRaceColumns, defaultSeries);
                }
                seriesToRaceColumns.get(defaultSeries).add(raceColumn);
            }
        }
        return seriesToRaceColumns;
    }

    private Series createDefaultSeries(Iterable<? extends Fleet> fleets) {
        Series defaultSeries;
        defaultSeries = new SeriesImpl(LeaderboardNameConstants.DEFAULT_SERIES_NAME, false, true,
                fleets, Collections.<String> emptyList(), null);
        return defaultSeries;
    }

    private List<RaceColumn> insertSeriesIfNew(Map<Series, List<RaceColumn>> target, Series newSeries) {
        if (!target.containsKey(newSeries)) {
            target.put(newSeries, new ArrayList<RaceColumn>());
        }
        return target.get(newSeries);
    }

}
