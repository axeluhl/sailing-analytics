package com.sap.sailing.domain.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.Test;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnInSeries;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.base.impl.SeriesImpl;
import com.sap.sailing.domain.base.impl.TrackedRaces;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.WindImpl;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.leaderboard.impl.FlexibleLeaderboardImpl;
import com.sap.sailing.domain.leaderboard.impl.LowPoint;
import com.sap.sailing.domain.leaderboard.impl.ThresholdBasedResultDiscardingRuleImpl;
import com.sap.sailing.domain.ranking.OneDesignRankingMetric;
import com.sap.sailing.domain.tracking.RaceExecutionOrderProvider;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRaceImpl;
import com.sap.sailing.domain.tracking.impl.TrackedRaceImpl;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * Covers the linking and unlinking between {@link RaceColumn}s and {@link TrackedRaces}s
 * and tests whether {@link RaceExecutionOrderProvider}s get correctly attached and detached. Also tests
 * late attaching and detaching of {@link RaceExecutionOrderProvider}s in case the {@link RaceExecutionOrderProvider}
 * was <code>null</code> when {@link RaceColumn} was linked to {@link TrackedRace}.
 * 
 * @author Alexander Ries (D062114)
 *
 */
public class RaceExecutionOrderProvdiderAttachDetachTest extends TrackBasedTest {
    private FlexibleLeaderboard flexibleLeaderboard;
    private RaceColumnInSeries raceColumnInSeries;
    private Fleet fleet;
    private TrackedRaceImpl trackedRace;
    private Regatta regatta;
    private Series series;

    private final String REGATTA = "TestRegatta";
    private final String RACE = "TestRace";
    private final String FLEET = "TestFleet";
    private final String BOATCLASS = "TestClass";
    private final String SERIES = "TestSeries";
    private final String FLEXIBLELEADERBOARD = "TestFlexibleLeaderboard";
    private final String RACECOLUMN_SERIES = "TestSeriesRaceColumn";
    private final String RACECOLUMN_FLEXIBLELEADERBOARD = "TestFlexibleLeadrboarRaceColumn";

    @Test
    public void testRaceExecutionOrderProviderAttachDetachWithRaceCollumn() {
        trackedRace = createTestTrackedRace(REGATTA, RACE, BOATCLASS, Collections.<Competitor> emptyList(),
                MillisecondsTimePoint.now());
        flexibleLeaderboard = new FlexibleLeaderboardImpl(FLEXIBLELEADERBOARD,
                new ThresholdBasedResultDiscardingRuleImpl(new int[] { 3, 6 }), new LowPoint(), null);
        flexibleLeaderboard.addRace(trackedRace, RACECOLUMN_FLEXIBLELEADERBOARD, false);
        assertTrue(trackedRace.hasRaceExecutionOrderProvidersAttached());
        flexibleLeaderboard.removeRaceColumn(RACECOLUMN_FLEXIBLELEADERBOARD);
        assertFalse(trackedRace.hasRaceExecutionOrderProvidersAttached());
    }

    @Test
    public void testWindInRegularIntervalWithPreviousRaceStillTracking() {
        final TimePoint startOfFirstRace = MillisecondsTimePoint.now();
        final TimePoint startOfSecondRace = startOfFirstRace.plus(Duration.ONE_MINUTE.times(5));
        DynamicTrackedRaceImpl previousTrackedRace = createTestTrackedRace(REGATTA, RACE, BOATCLASS, Collections.<Competitor> emptyList(), startOfFirstRace);
        previousTrackedRace.setStartOfTrackingReceived(startOfFirstRace);
        trackedRace = createTestTrackedRace(REGATTA, "TestRace2", BOATCLASS, Collections.<Competitor> emptyList(), startOfSecondRace);
        flexibleLeaderboard = new FlexibleLeaderboardImpl(FLEXIBLELEADERBOARD,
                new ThresholdBasedResultDiscardingRuleImpl(new int[] { 3, 6 }), new LowPoint(), null);
        flexibleLeaderboard.addRace(previousTrackedRace, RACECOLUMN_FLEXIBLELEADERBOARD, false);
        flexibleLeaderboard.addRace(trackedRace, RACECOLUMN_FLEXIBLELEADERBOARD, false);
        Wind wind = new WindImpl(new DegreePosition(12, 13), startOfSecondRace.plus(Duration.ONE_MINUTE), new KnotSpeedWithBearingImpl(
                /* speedInKnots */18, new DegreeBearingImpl(185)));
        assertTrue(previousTrackedRace.takesWindFix(wind)); // previous race has tracking still open and takes the fix
        assertTrue(trackedRace.takesWindFix(wind)); // tracked race also needs to take the fix as it falls into the regular tracking interval
    }

    @Test
    public void testRaceExecutionOrderProviderAttachDetachWithRaceCollumnInSeries() {
        createTestSetupWithRegattaAndSeries(/* linkSeriesToRegatta */true);
        raceColumnInSeries.setTrackedRace(fleet, trackedRace);
        assertTrue(trackedRace.hasRaceExecutionOrderProvidersAttached());
        raceColumnInSeries.releaseTrackedRace(fleet);
        assertFalse(trackedRace.hasRaceExecutionOrderProvidersAttached());
    }

    @Test
    public void testRaceExecutionOrderProviderAttachDetachWhenSeriesRegattaIsSetAndRemovedAfterTrackedRaceHaveBeenSetToRaceColumns() {
        createTestSetupWithRegattaAndSeries(/* linkSeriesToRegatta */false);
        raceColumnInSeries.setTrackedRace(fleet, trackedRace);
        assertFalse(trackedRace.hasRaceExecutionOrderProvidersAttached());
        series.setRegatta(regatta);
        assertTrue(trackedRace.hasRaceExecutionOrderProvidersAttached());
        series.setRegatta(null);
        assertFalse(trackedRace.hasRaceExecutionOrderProvidersAttached());
    }

    private void createTestSetupWithRegattaAndSeries(boolean linkSeriesToRegatta) {
        trackedRace = createTestTrackedRace(REGATTA, RACE, BOATCLASS, Collections.<Competitor> emptyList(),
                MillisecondsTimePoint.now());
        fleet = new FleetImpl(FLEET);
        Set<Fleet> fleets = new HashSet<>();
        fleets.add(fleet);
        series = new SeriesImpl(SERIES, false, fleets, new HashSet<String>(), null);
        Set<Series> seriesSet = new HashSet<>();
        if (linkSeriesToRegatta) {
            seriesSet.add(series);
        }
        BoatClass boatClass = new BoatClassImpl(BOATCLASS, true);
        raceColumnInSeries = series.addRaceColumn(RACECOLUMN_SERIES, null);
        ScoringScheme scoringScheme = new LowPoint();
        regatta = new RegattaImpl(RegattaImpl.getDefaultName(REGATTA, boatClass.getName()), boatClass,
                /* startDate */null, /* endDate */null, seriesSet, false, scoringScheme, UUID.randomUUID(), null,
                OneDesignRankingMetric::new);
    }
}
