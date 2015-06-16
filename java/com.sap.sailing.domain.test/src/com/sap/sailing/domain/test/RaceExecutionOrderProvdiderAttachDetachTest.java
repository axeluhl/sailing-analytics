package com.sap.sailing.domain.test;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnInSeries;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.impl.AbstractRaceExecutionOrderProvider;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.base.impl.SeriesImpl;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.FlexibleRaceColumn;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.leaderboard.impl.FlexibleLeaderboardImpl;
import com.sap.sailing.domain.leaderboard.impl.LowPoint;
import com.sap.sailing.domain.leaderboard.impl.ThresholdBasedResultDiscardingRuleImpl;
import com.sap.sailing.domain.ranking.OneDesignRankingMetric;
import com.sap.sailing.domain.tracking.RaceExecutionOrderProvider;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;
import com.sap.sailing.domain.tracking.impl.TrackedRaceImpl;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * @author Alexander Ries (D062114)
 *
 */
public class RaceExecutionOrderProvdiderAttachDetachTest extends TrackBasedTest {
    
    RaceExecutionOrderProvider raceExecutionOrderProvider;
    FlexibleLeaderboard flexibleLeaderboard;

    RaceColumnInSeries raceColumnInSeries;
    FlexibleRaceColumn raceColumnFlexibleLeaderboard;
    Fleet fleet;
    
    TrackedRaceImpl trackedRace;
    Regatta regatta;
    Series series;
    private final String REGATTA = "TestRegatta";
    private final String RACE = "TestRace";
    private final String FLEET = "TestFleet";
    private final String BOATCLASS = "49er";
    private final String RACECOLUMN_SERIES = "SeriesRaceColumn";
    private final String RACECOLUMN_FLEXIBLELEADERBOARD = "FlexibleLeadrboarRaceColumn";

    @Before
    public void setUp() {
        setUpForAllTestCases();
        setUpForFlexibleLeaderboardRaceColumnsTests();
        setUpRaceColumnInSeriesTest();
    }

    @Test
    public void testRaceExecutionOrderProviderAttachWhenSettingTrackedRaceToRaceCollumn() {
        flexibleLeaderboard.addRace(trackedRace, RACECOLUMN_FLEXIBLELEADERBOARD, false);
        System.out.println(""+trackedRace.hasRaceExecutionOrderProvidersAttached());
        assertTrue(trackedRace.hasRaceExecutionOrderProvidersAttached());
    }

    @Test
    public void testRaceExecutionOrderProviderDetachWhenReleasingTrackedRaceFromRaceCollumn() {
        flexibleLeaderboard.removeRaceColumn(RACECOLUMN_FLEXIBLELEADERBOARD);
        assertFalse(trackedRace.hasRaceExecutionOrderProvidersAttached());
    }

    @Test
    public void testRaceExecutionOrderProviderAttachWhenSettingTrackedRaceToRaceCollumnInSeries() {
        raceColumnInSeries.setTrackedRace(fleet, trackedRace);
        assertTrue(trackedRace.hasRaceExecutionOrderProvidersAttached());
    }

    @Test
    public void testRaceExecutionOrderProviderDetachWhenReleasingTrackedRaceFromRaceCollumnInSeries() {
        raceColumnInSeries.releaseTrackedRace(fleet);
        assertFalse(trackedRace.hasRaceExecutionOrderProvidersAttached());
    }
    
    private void setUpForAllTestCases(){
        final List<Competitor> emptyCompetitorList = Collections.emptyList();
        TimePoint now = MillisecondsTimePoint.now();
        trackedRace = createTestTrackedRace(REGATTA, RACE, BOATCLASS, emptyCompetitorList, now);
        raceExecutionOrderProvider = new AbstractRaceExecutionOrderProvider() {
            private static final long serialVersionUID = 1L;

            @Override
            protected Map<Fleet, Iterable<? extends RaceColumn>> getRaceColumnsOfSeries() {
                return Collections.<Fleet, Iterable<? extends RaceColumn>>emptyMap();
            }
        };
    }
    
    private void setUpForFlexibleLeaderboardRaceColumnsTests(){
        flexibleLeaderboard = new FlexibleLeaderboardImpl("Kiel Week 2011 505s", new ThresholdBasedResultDiscardingRuleImpl(new int[] { 3, 6 }),
                new LowPoint(), null);
    }
    
    private void setUpRaceColumnInSeriesTest(){
      fleet = new FleetImpl(FLEET);
      series = createSeries(null, fleet);
      Set<Series> seriesSet = new HashSet<>();
      seriesSet.add(series);
      BoatClass boatClass = new BoatClassImpl(BOATCLASS, true);
      String raceColumnName = RACECOLUMN_SERIES;
      raceColumnInSeries = series.addRaceColumn(raceColumnName, null);
      ScoringScheme scoringScheme = new LowPoint();
      regatta = new RegattaImpl(RegattaImpl.getDefaultName(REGATTA, boatClass.getName()), boatClass,
      /* startDate */null, /* endDate */null, seriesSet, false, scoringScheme, UUID.randomUUID(),
              null, OneDesignRankingMetric::new);
    }

    private Series createSeries(TrackedRegattaRegistry trackedRegattaRegistry, Fleet fleet) {
        Set<Fleet> fleets = new HashSet<>();
        fleets.add(fleet);
        Series series = new SeriesImpl("TestSeries", false, fleets, new HashSet<String>(), trackedRegattaRegistry);
        return series;
    }
}
