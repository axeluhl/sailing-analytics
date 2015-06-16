package com.sap.sailing.domain.test;

import static org.mockito.Mockito.mock;

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
import com.sap.sailing.domain.base.CourseArea;
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
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.leaderboard.impl.LowPoint;
import com.sap.sailing.domain.ranking.OneDesignRankingMetric;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.RaceExecutionOrderProvider;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * @author Alexander Ries (D062114)
 *
 */
public class RaceExecutionOrderProvdiderAttachDetachTest extends TrackBasedTest {

    RaceColumn raceColumn;
    Series series;
    Fleet fleet;
    RaceColumnInSeries raceColumnInSeries;
    Regatta regatta;
    DynamicTrackedRace dynamicTackedRace;
    RaceExecutionOrderProvider raceExecutionOrderProvider;

    @Before
    public void setUp() {
        final List<Competitor> emptyCompetitorList = Collections.emptyList();
        TimePoint now = MillisecondsTimePoint.now();
        dynamicTackedRace = createTestTrackedRace("TestRegatta", "TestRace", "TestBoatClass", emptyCompetitorList, now);
        raceExecutionOrderProvider = new AbstractRaceExecutionOrderProvider() {

            private static final long serialVersionUID = 1L;

            @Override
            protected Map<Fleet, Iterable<? extends RaceColumn>> getRaceColumnsOfSeries() {
                return null;
            }
        };
        TrackedRegattaRegistry trackedRegattaRegistry = mock(TrackedRegattaRegistry.class);
        fleet = new FleetImpl("TestFleet");
        Series series = createSeries(trackedRegattaRegistry, fleet);
        Set<Series> seriesSet = new HashSet<>();
        seriesSet.add(series);
        BoatClass boatClass = new BoatClassImpl("TestClass", true);
        String raceColumnName = "TestRace";
        raceColumnInSeries = series.addRaceColumn(raceColumnName, trackedRegattaRegistry);
        ScoringScheme scoringScheme = new LowPoint();
        regatta = new RegattaImpl(RegattaImpl.getDefaultName("TestRegatta", boatClass.getName()), boatClass,
        /* startDate */null, /* endDate */null, seriesSet, false, scoringScheme, UUID.randomUUID(),
                mock(CourseArea.class), OneDesignRankingMetric::new);
    }

//    @Test
//    public void testRaceExecutionOrderProviderAttachWhenSettingTrackedRaceToRaceCollumn() {
//
//    }
//
//    @Test
//    public void testRaceExecutionOrderProviderDetachWhenReleasingTrackedRaceFromRaceCollumn() {
//
//    }

    @Test
    public void testRaceExecutionOrderProviderAttachWhenSettingTrackedRaceToRaceCollumnInSeries() {
        raceColumnInSeries.setTrackedRace(fleet, dynamicTackedRace);
    }

    @Test
    public void testRaceExecutionOrderProviderDetachWhenReleasingTrackedRaceFromRaceCollumnInSeries() {
        raceColumnInSeries.releaseTrackedRace(fleet);
    }

    private Series createSeries(TrackedRegattaRegistry trackedRegattaRegistry, Fleet fleet) {
        Set<Fleet> fleets = new HashSet<>();
        fleets.add(fleet);
        Series series = new SeriesImpl("TestSeries", false, fleets, new HashSet<String>(), trackedRegattaRegistry);
        return series;
    }
}
