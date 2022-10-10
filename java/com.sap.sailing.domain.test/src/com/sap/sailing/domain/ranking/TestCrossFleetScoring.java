package com.sap.sailing.domain.ranking;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorWithBoat;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.Sideline;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.ControlPointWithTwoMarksImpl;
import com.sap.sailing.domain.base.impl.CourseImpl;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.base.impl.MarkImpl;
import com.sap.sailing.domain.base.impl.RaceDefinitionImpl;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.base.impl.SeriesImpl;
import com.sap.sailing.domain.base.impl.WaypointImpl;
import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.domain.common.CompetitorRegistrationType;
import com.sap.sailing.domain.common.RankingMetrics;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.WindImpl;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.common.tracking.impl.GPSFixImpl;
import com.sap.sailing.domain.common.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.racelog.RaceLogAndTrackedRaceResolver;
import com.sap.sailing.domain.racelog.impl.EmptyRaceLogStore;
import com.sap.sailing.domain.regattalog.impl.EmptyRegattaLogStore;
import com.sap.sailing.domain.test.LeaderboardScoringAndRankingTestBase;
import com.sap.sailing.domain.test.TrackBasedTest;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRaceImpl;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRegattaImpl;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.domain.tracking.impl.MarkPassingImpl;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.DegreeBearingImpl;
import com.sap.sse.common.impl.MillisecondsDurationImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class TestCrossFleetScoring extends LeaderboardScoringAndRankingTestBase {
    private final BoatClass boatClass = new BoatClassImpl(BoatClassMasterdata.PIRATE);
    private final TimePoint referenceTimePoint = MillisecondsTimePoint.now();
    private Leaderboard leaderboard;
    private RankingMetric rankingMetric;
    private final DynamicTrackedRace[] trackedRaces = new DynamicTrackedRace[2]; // first race Yellow second race Blue
    private CompetitorWithBoat c1Yellow, c2Yellow, c1Blue, c2Blue;
    private Waypoint start;
    private Waypoint windward;
    private Waypoint finish;
    private Waypoint left;

    @Before
    public void before() {
        setUp(c -> c.getName().contains("c1") ? 2.0 : 1.0, c -> 0.0);
    }

    private void setUp(TimeOnTimeFactorMapping timeOnTimeFactors, Function<Competitor, Double> timeOnDistanceFactors) {
        final List<Fleet> fleets = new ArrayList<>();
        final ArrayList<Series> series = new ArrayList<>();

        // create two Competitors per fleet
        c1Yellow = TrackBasedTest.createCompetitorWithBoat("FastYellowBoat");
        c2Yellow = TrackBasedTest.createCompetitorWithBoat("SlowYellowBoat");
        c1Blue = TrackBasedTest.createCompetitorWithBoat("FastBlueBoat");
        c2Blue = TrackBasedTest.createCompetitorWithBoat("SlowBlueBoat");

        for (String FleetName : new String[] { "Yellow", "Blue" }) {
            fleets.add(new FleetImpl(FleetName, 0));
        }
        final List<String> raceColumnNames = new ArrayList<>();
        raceColumnNames.add("R1");
        final Series zeroRankSeries = new SeriesImpl("zero Rank", /* isMedal */false,
                /* isFleetsCanRunInParallel */ true, fleets, raceColumnNames, /* trackedRegattaRegistry */ null);
        zeroRankSeries.setCrossFleetMergedRanking(true);
        series.add(zeroRankSeries);
        final Regatta regatta = new RegattaImpl(RegattaImpl.getDefaultName("Test Regatta", boatClass.getName()),
                boatClass, /* canBoatsOfCompetitorsChangePerRace */ true, CompetitorRegistrationType.CLOSED,
                /* startDate */ null, /* endDate */ null, series, /* persistent */false,
                DomainFactory.INSTANCE.createScoringScheme(ScoringSchemeType.LOW_POINT), "123", /* course area */null,
                OneDesignRankingMetric::new, /* registrationLinkSecret */ UUID.randomUUID().toString());

        TrackedRegatta trackedRegatta = new DynamicTrackedRegattaImpl(regatta);
        leaderboard = createLeaderboard(regatta, /* discarding thresholds */ new int[0]);

        // create a two-lap upwind/downwind course:
        List<Waypoint> waypoints = new ArrayList<Waypoint>();
        MarkImpl left = new MarkImpl("Left lee gate buoy");
        MarkImpl right = new MarkImpl("Right lee gate buoy");
        ControlPoint leeGate = new ControlPointWithTwoMarksImpl(left, right, "Lee Gate", "Lee Gate");
        Mark windwardMark = new MarkImpl("Windward mark");
        start = new WaypointImpl(leeGate);
        waypoints.add(start);
        windward = new WaypointImpl(windwardMark);
        waypoints.add(windward);
        finish = new WaypointImpl(leeGate);
        waypoints.add(finish);
        Course course = new CourseImpl("Test Course", waypoints);

        // create two tracked races
        for (int i = 0; i < 2; i++) {
            final RaceColumn r1Column = series.get(0).getRaceColumnByName("R1");
            Map<Competitor, Boat> competitorsAndBoats;
            Fleet fleet;
            if (i == 0) {
                fleet = r1Column.getFleetByName("Yellow");
                competitorsAndBoats = TrackBasedTest.createCompetitorAndBoatsMap(c1Yellow, c2Yellow);
            } else {
                fleet = r1Column.getFleetByName("Blue");
                competitorsAndBoats = TrackBasedTest.createCompetitorAndBoatsMap(c1Blue, c2Blue);
            }
            RaceDefinition race = new RaceDefinitionImpl("Test Race", course, boatClass, competitorsAndBoats);
            DynamicTrackedRaceImpl trackedRace = new DynamicTrackedRaceImpl(trackedRegatta, race,
                    Collections.<Sideline> emptyList(), EmptyWindStore.INSTANCE, /* delayToLiveInMillis */ 0,
                    /* millisecondsOverWhichToAverageWind */ 30000, /* millisecondsOverWhichToAverageSpeed */ 30000,
                    /* delay for wind estimation cache invalidation */ 0, /* useMarkPassingCalculator */ false,
                    tr -> new TimeOnTimeAndDistanceRankingMetric(tr, timeOnTimeFactors, // time-on-time
                            c -> new MillisecondsDurationImpl((long) (1000. * timeOnDistanceFactors.apply(c)))),
                    mock(RaceLogAndTrackedRaceResolver.class), null);
            // in this simplified artificial course, the top mark is exactly north of the right leeward gate
            DegreePosition topPosition = new DegreePosition(1, 0);
            trackedRace.getOrCreateTrack(left)
                    .addGPSFix(new GPSFixImpl(new DegreePosition(0, -0.000001), referenceTimePoint));
            trackedRace.getOrCreateTrack(right)
                    .addGPSFix(new GPSFixImpl(new DegreePosition(0, 0.000001), referenceTimePoint));
            trackedRace.getOrCreateTrack(windwardMark).addGPSFix(new GPSFixImpl(topPosition, referenceTimePoint));
            trackedRace.recordWind(
                    new WindImpl(topPosition, referenceTimePoint,
                            new KnotSpeedWithBearingImpl(/* speedInKnots */14.7, new DegreeBearingImpl(180))),
                    new WindSourceImpl(WindSourceType.WEB));
            assertEquals(120, trackedRace.getCourseLength().getNauticalMiles(), 0.02);
            r1Column.setTrackedRace(fleet, trackedRace);
            trackedRaces[i] = trackedRace;
        }
    }

    private void testOnStartLeg(Competitor expectedOrder[], double sailedDistance[]) {
        testWithCourseCreation(expectedOrder, sailedDistance, null);
    }

    private void testWithCourseCreation(Competitor expectedOrder[], double sailedDistance[],
            List<Competitor> markroundingForFirstMark) {
        TimePoint startOfRace, markRounding, timePointOfViewingTheLeaderboard = null;

        for (int i = 0; i < trackedRaces.length; i++) {
            startOfRace = referenceTimePoint.plus(Duration.ONE_MINUTE.times(10).times(i));
            markRounding = startOfRace.plus(Duration.ONE_MINUTE.times(30));
            timePointOfViewingTheLeaderboard = startOfRace.plus(Duration.ONE_HOUR);
            DynamicTrackedRace trackedRace = trackedRaces[i];
            CompetitorWithBoat c1, c2;
            if (i == 0) {
                c1 = c1Yellow;
                c2 = c2Yellow;
            } else {
                c1 = c1Blue;
                c2 = c2Blue;
            }

            trackedRace.getTrack(c1).add(new GPSFixMovingImpl(new DegreePosition(0.0, 0), startOfRace,
                    new KnotSpeedWithBearingImpl(12, new DegreeBearingImpl(45))));
            trackedRace.getTrack(c2).add(new GPSFixMovingImpl(new DegreePosition(0.0, 0), startOfRace,
                    new KnotSpeedWithBearingImpl(12, new DegreeBearingImpl(315))));

            if (markroundingForFirstMark != null && markroundingForFirstMark.contains(c1)) {
                trackedRace.updateMarkPassings(c1, Arrays.<MarkPassing> asList(
                        new MarkPassingImpl(startOfRace, start, c1), new MarkPassingImpl(markRounding, windward, c1)));
                trackedRace.getTrack(c1).add(new GPSFixMovingImpl(new DegreePosition(1.0, 0), markRounding,
                        new KnotSpeedWithBearingImpl(12, new DegreeBearingImpl(180))));
            } else {
                trackedRace.updateMarkPassings(c1,
                        Arrays.<MarkPassing> asList(new MarkPassingImpl(startOfRace, start, c1)));
            }
            if (markroundingForFirstMark != null && markroundingForFirstMark.contains(c2)) {
                trackedRace.updateMarkPassings(c2, Arrays.<MarkPassing> asList(
                        new MarkPassingImpl(startOfRace, start, c2), new MarkPassingImpl(markRounding, windward, c2)));
                trackedRace.getTrack(c2).add(new GPSFixMovingImpl(new DegreePosition(1.0, 0), markRounding,
                        new KnotSpeedWithBearingImpl(12, new DegreeBearingImpl(180))));
            } else {
                trackedRace.updateMarkPassings(c2,
                        Arrays.<MarkPassing> asList(new MarkPassingImpl(startOfRace, start, c2)));
            }

            if (i == 0) {
                trackedRace.getTrack(c1).add(new GPSFixMovingImpl(new DegreePosition(sailedDistance[0], 0),
                        timePointOfViewingTheLeaderboard, new KnotSpeedWithBearingImpl(12, new DegreeBearingImpl(45))));
                trackedRace.getTrack(c2)
                        .add(new GPSFixMovingImpl(new DegreePosition(sailedDistance[1], 0),
                                timePointOfViewingTheLeaderboard,
                                new KnotSpeedWithBearingImpl(12, new DegreeBearingImpl(315))));
            } else {
                trackedRace.getTrack(c1).add(new GPSFixMovingImpl(new DegreePosition(sailedDistance[2], 0),
                        timePointOfViewingTheLeaderboard, new KnotSpeedWithBearingImpl(12, new DegreeBearingImpl(45))));
                trackedRace.getTrack(c2)
                        .add(new GPSFixMovingImpl(new DegreePosition(sailedDistance[3], 0),
                                timePointOfViewingTheLeaderboard,
                                new KnotSpeedWithBearingImpl(12, new DegreeBearingImpl(315))));
            }

        }
        // Both boats have climbed half of the first upwind beat; c1 is rated the faster boat (2.0), c2 has time-on-time
        // factor 1.0.
        // Therefore, c2 is expected to lead within a fleet after applying the corrections. In total the Blue fleet
        // leads
        final List<Competitor> rankedCompetitors = leaderboard
                .getCompetitorsFromBestToWorst(timePointOfViewingTheLeaderboard);
        assertEquals(expectedOrder[0].getName(), rankedCompetitors.get(0).getName());
        assertEquals(expectedOrder[1].getName(), rankedCompetitors.get(1).getName());
        assertEquals(expectedOrder[2].getName(), rankedCompetitors.get(2).getName());
        assertEquals(expectedOrder[3].getName(), rankedCompetitors.get(3).getName());
    }

    /*
     * The fleet launched second has completely overtaken the fleet launched first. The distance between the fleets is
     * so large that the fleet that started second is in front. Within the fleets, c1 and c2 have sailed the same
     * distance, so c2 is ahead due to the ToT factor. This leads to the following ranking: c2Blue, c1Blue, c2Yellow,
     * c1Yellow
     */
    @Test
    public void testTimeOnTimeWithFactorSecondFleetOvertookFirstFleetCompetitorsInFleetHaveSameSailedDistance() {
        Competitor[] expectedOrder = new Competitor[] { c2Blue, c1Blue, c2Yellow, c1Yellow };
        double[] sailedDistance = new double[] { 0.2, 0.2, 0.75, 0.75 };
        testOnStartLeg(expectedOrder, sailedDistance);
    }

    /*
     * The fleet launched second has completely overtaken the fleet launched first. The distance between the fleets is
     * so large that the fleet that started second is in front. Within the fleets, c2 has covered minimally less
     * distance than c1. However, due to the ToT factor, c2 is still ahead of c1. This leads to the following ranking:
     * c2Blue, c1Blue, c2Yellow, c1Yellow
     */

    @Test
    public void testTimeOnTimeWithFactorSecondFleetOvertookFirstFleetCompetitorsInFleetHaveDifferentSailedDistancesC2Leeds() {
        Competitor[] expectedOrder = new Competitor[] { c2Blue, c1Blue, c2Yellow, c1Yellow };
        double[] sailedDistance = new double[] { 0.1, 0.19, 0.75, 0.74 };
        testOnStartLeg(expectedOrder, sailedDistance);
    }

    /*
     * The competitor c1Blue has a large distance advantage over the other competitors. While c2Blue sails just behind
     * c1Yellow. c2Yewllow is again spatially behind c2Blue. Due to the ToT factors the following ranking results:
     * c1Blue,c2Blue, c1Yellow, c2Yewllow
     */

    @Test
    public void testTimeOnTimeWithFactorSecondFleetOvertookFirstFleetCompetitorsInFleetHaveDifferentSailedDistancesC1Leads() {
        Competitor[] expectedOrder = new Competitor[] { c1Blue, c2Blue, c1Yellow, c2Yellow };
        double[] sailedDistance = new double[] { 0.4, 0.19, 0.75, 0.374 };
        testOnStartLeg(expectedOrder, sailedDistance);
    }

    /*
     * The competitors c1Yewllow and c1Blue as well as c2Yellow and c2Blue sailed the same distance. The distance
     * between c1 and c2 is such that the ToT factors put the c2 boats in the lead, resulting in the following ranking:
     * c2Yellow, c2Blue, c1Yellow, c1Blue
     */
    @Test
    public void testTimeOnTimeWithFactorC1BlueOvertookC1YellowAndC2BlueOvertookC2Yellow() {
        Competitor[] expectedOrder = new Competitor[] { c1Blue, c2Blue, c1Yellow, c2Yellow };
        double[] sailedDistance = new double[] { 0.5, 0.3, 0.5, 0.3 };
        testOnStartLeg(expectedOrder, sailedDistance);
    }

    /*
     * The competitors c1Yewllow and c1Blue as well as c2Yellow and c2Blue sailed the same distance. The distance
     * between c1 and c2 is such that the ToT factors put the c1 boats in the lead, resulting in the following ranking:
     * c1Yellow, c1Blue, c2Yellow, c2Blue
     */
    @Test
    public void testTimeOnTimeWithFactor2() {
        Competitor[] expectedOrder = new Competitor[] { c1Yellow, c2Yellow, c1Blue, c2Blue };
        System.out.println(c1Yellow.getName());
        double[] sailedDistance = new double[] { 0.5, 0.2, 0.5, 0.2 };
        testOnStartLeg(expectedOrder, sailedDistance);
    }

    /*
     * c1Blue has overtaken c2Yellow. However, C2Blue is still behind c2Yewllow, resulting in the following order:
     * c1Yellow, c1Blue, c2Yellow, c2Blue
     */
    @Test
    public void testTimeOnTimeWithFactorC1BlueOvertookC2Yellow() {
        Competitor[] expectedOrder = new Competitor[] { c1Yellow, c1Blue, c2Yellow, c2Blue };
        double[] sailedDistance = new double[] { 0.5, 0.2, 0.3, 0.12 };
        testOnStartLeg(expectedOrder, sailedDistance);
    }

    /*
     * c1Blue has caught up to c2Yellow enough to lead to an overtake on the leaderboard. C2Blue continues behind
     * c2Yewllow, resulting in the following order:
     */

    @Test
    public void testTimeOnTimeWithFactor3() {
        Competitor[] expectedOrder = new Competitor[] { c1Yellow, c1Blue, c2Yellow, c2Blue };
        double[] sailedDistance = new double[] { 0.5, 0.2, 0.19, 0.09 };
        testOnStartLeg(expectedOrder, sailedDistance);
    }

    /*
     * Just a normal race without overtaking
     */
    @Test
    public void testTimeOnTimeWithFactor5() {
        Competitor[] expectedOrder = new Competitor[] { c1Blue, c1Yellow, c2Blue, c2Yellow };
        double[] sailedDistance = new double[] { 0.5, 0.1, 0.5, 0.1 };
        testOnStartLeg(expectedOrder, sailedDistance);
    }

    /*
     * First Fleet has already rounded the mark and leads. While c1 is ahead of c2
     */
    @Test
    public void testTimeOnTimeFirstFleetRoundedMarkAndLeads() {
        Competitor[] expectedOrder = new Competitor[] { c1Yellow, c2Yellow,  c1Blue, c2Blue };
        double[] sailedDistance = new double[] { 0.4, 0.9, 0.5, 0.1 };
        List<Competitor> markRoundings = new ArrayList<>(); 
        markRoundings.add(c1Yellow); 
        markRoundings.add(c2Yellow); 
        testWithCourseCreation(expectedOrder, sailedDistance, markRoundings);
    }
    
    /*
     * Both Fleets have rounded the first mark no overtaking took place. The ranking has not changed either. 
     */
    @Test
    public void testTimeOnTimeBothFleetsRoundedMarkAndNoOvertaking() {
        TimePoint startOfRace, markRounding, timePointOfViewingTheLeaderboard = null;

        for (int i = 0; i < trackedRaces.length; i++) {
            startOfRace = referenceTimePoint.plus(Duration.ONE_MINUTE.times(10).times(i));
            markRounding = startOfRace.plus(Duration.ONE_MINUTE.times(30));
            timePointOfViewingTheLeaderboard = markRounding.plus(Duration.ONE_MINUTE.times(5));
            DynamicTrackedRace trackedRace = trackedRaces[i];
            CompetitorWithBoat c1, c2;
            if (i == 0) {
                c1 = c1Yellow;
                c2 = c2Yellow;
            } else {
                c1 = c1Blue;
                c2 = c2Blue;
            }

            trackedRace.getTrack(c1).add(new GPSFixMovingImpl(new DegreePosition(0.0, 0), startOfRace,
                    new KnotSpeedWithBearingImpl(12, new DegreeBearingImpl(45))));
            trackedRace.getTrack(c2).add(new GPSFixMovingImpl(new DegreePosition(0.0, 0), startOfRace,
                    new KnotSpeedWithBearingImpl(12, new DegreeBearingImpl(315))));

            if (i == 0) {
                // mark rounding c1Yellow
                trackedRace.updateMarkPassings(c1, Arrays.<MarkPassing> asList(
                        new MarkPassingImpl(startOfRace, start, c1), new MarkPassingImpl(markRounding, windward, c1)));
                trackedRace.getTrack(c1).add(new GPSFixMovingImpl(new DegreePosition(1.0, 0), markRounding,
                        new KnotSpeedWithBearingImpl(12, new DegreeBearingImpl(180))));
                // mark rounding c2Yellow
                trackedRace.updateMarkPassings(c2, Arrays.<MarkPassing> asList(
                        new MarkPassingImpl(startOfRace, start, c2), new MarkPassingImpl(markRounding, windward, c2)));
                trackedRace.getTrack(c2).add(new GPSFixMovingImpl(new DegreePosition(1.0, 0), markRounding,
                        new KnotSpeedWithBearingImpl(12, new DegreeBearingImpl(180))));
                //current position
                trackedRace.getTrack(c1).add(new GPSFixMovingImpl(new DegreePosition(0.4, 0),
                        timePointOfViewingTheLeaderboard, new KnotSpeedWithBearingImpl(12, new DegreeBearingImpl(45))));
                trackedRace.getTrack(c2)
                        .add(new GPSFixMovingImpl(new DegreePosition(0.9, 0),
                                timePointOfViewingTheLeaderboard,
                                new KnotSpeedWithBearingImpl(12, new DegreeBearingImpl(315))));
            } else {
                trackedRace.getTrack(c1).add(new GPSFixMovingImpl(new DegreePosition(0.95, 0),
                        timePointOfViewingTheLeaderboard, new KnotSpeedWithBearingImpl(12, new DegreeBearingImpl(45))));
                trackedRace.getTrack(c2)
                        .add(new GPSFixMovingImpl(new DegreePosition(0.4, 0),
                                timePointOfViewingTheLeaderboard,
                                new KnotSpeedWithBearingImpl(12, new DegreeBearingImpl(315))));
            }

        }
        // Both boats have climbed half of the first upwind beat; c1 is rated the faster boat (2.0), c2 has time-on-time
        // factor 1.0.
        // Therefore, c2 is expected to lead within a fleet after applying the corrections. In total the Blue fleet
        // leads
        final List<Competitor> rankedCompetitors = leaderboard
                .getCompetitorsFromBestToWorst(timePointOfViewingTheLeaderboard);
        assertEquals(c1Blue.getName(), rankedCompetitors.get(0).getName());
        assertEquals(c2Blue.getName(), rankedCompetitors.get(1).getName());
        assertEquals(c1Yellow.getName(), rankedCompetitors.get(2).getName());
        assertEquals(c2Yellow.getName(), rankedCompetitors.get(3).getName());
    }
}
