package com.sap.sailing.domain.ranking;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
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
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.common.impl.DegreeBearingImpl;
import com.sap.sse.common.impl.MillisecondsDurationImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class TestCrossFleetScoring extends LeaderboardScoringAndRankingTestBase {
    private final BoatClass boatClass = new BoatClassImpl(BoatClassMasterdata.PIRATE);
    private final TimePoint referenceTimePoint = MillisecondsTimePoint.now();
    private Leaderboard leaderboard;

    private Waypoint start;
    private Waypoint windward;
    private Waypoint finish;
    private final Map<DynamicTrackedRace, List<CompetitorWithBoat>> trackedRaces = new HashMap<>();
    final Map<Fleet, List<CompetitorWithBoat>> fleets = new HashMap<>();
    private final Map<String, CompetitorWithBoat> competitors = new HashMap<>();

    private void setUp(TimeOnTimeFactorMapping timeOnTimeFactors,
            Function<Competitor, Double> timeOnDistanceAllowance) {
        final ArrayList<Series> series = new ArrayList<>();
        for (String FleetName : new String[] { "Yellow", "Blue" }) {
            CompetitorWithBoat c1 = TrackBasedTest.createCompetitorWithBoat("Fast" + FleetName + "Boat");
            CompetitorWithBoat c2 = TrackBasedTest.createCompetitorWithBoat("Slow" + FleetName + "Boat");
            List<CompetitorWithBoat> competitorsForFleet = Arrays.asList(c1, c2);
            fleets.put(new FleetImpl(FleetName, 0), competitorsForFleet);
            competitors.putAll(competitorsForFleet.stream()
                    .collect(Collectors.toMap(Competitor::getName, competitor -> competitor)));

        }
        final List<String> raceColumnNames = new ArrayList<>();
        raceColumnNames.add("R1");
        final Series zeroRankSeries = new SeriesImpl("zero Rank", /* isMedal */false,
                /* isFleetsCanRunInParallel */ true, fleets.keySet(), raceColumnNames,
                /* trackedRegattaRegistry */ null);
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

        for (Map.Entry<Fleet, List<CompetitorWithBoat>> fleetAndCompetitors : fleets.entrySet()) {
            final RaceColumn r1Column = series.get(0).getRaceColumnByName("R1");
            final Map<Competitor, Boat> competitorsAndBoats = TrackBasedTest
                    .createCompetitorAndBoatsMap(fleetAndCompetitors.getValue()
                            .toArray(new CompetitorWithBoat[fleetAndCompetitors.getValue().size()]));
            RaceDefinition race = new RaceDefinitionImpl("Test Race", course, boatClass, competitorsAndBoats);
            DynamicTrackedRaceImpl trackedRace = new DynamicTrackedRaceImpl(trackedRegatta, race,
                    Collections.<Sideline> emptyList(), EmptyWindStore.INSTANCE, /* delayToLiveInMillis */ 0,
                    /* millisecondsOverWhichToAverageWind */ 30000, /* millisecondsOverWhichToAverageSpeed */ 30000,
                    /* delay for wind estimation cache invalidation */ 0, /* useMarkPassingCalculator */ false,
                    tr -> new TimeOnTimeAndDistanceRankingMetric(tr, timeOnTimeFactors, // time-on-time
                            c -> new MillisecondsDurationImpl((long) (1000. * timeOnDistanceAllowance.apply(c)))),
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
            r1Column.setTrackedRace(fleetAndCompetitors.getKey(), trackedRace);
            trackedRaces.put(trackedRace, fleetAndCompetitors.getValue());
        }
    }

    private void testOnStartLeg(String[] expectedCompetitorOrder,
            Map<Competitor, Pair<List<MarkPassing>, List<GPSFixMovingImpl>>> competitorsAndMarkPassingsWithGpsFixes,
            TimePoint timePointOfViewingTheLeaderboard) {

        for (Map.Entry<DynamicTrackedRace, List<CompetitorWithBoat>> trackedRaceAndCompetitors : trackedRaces
                .entrySet()) {
            DynamicTrackedRace trackedRace = trackedRaceAndCompetitors.getKey();
            for (Competitor competitor : trackedRaceAndCompetitors.getValue()) {
                trackedRace.updateMarkPassings(competitor,
                        competitorsAndMarkPassingsWithGpsFixes.get(competitor).getA());
                List<GPSFixMovingImpl> gpsPositions = competitorsAndMarkPassingsWithGpsFixes.get(competitor).getB();
                for (GPSFixMovingImpl gpsPosition : gpsPositions) {
                    trackedRace.getTrack(competitor).add(gpsPosition);
                }
            }

        }

        final List<Competitor> rankedCompetitors = leaderboard
                .getCompetitorsFromBestToWorst(timePointOfViewingTheLeaderboard);
        Iterator<Competitor> it = rankedCompetitors.iterator();
        for (String currentCompetitor : expectedCompetitorOrder) {
            if (it.hasNext())
                assertEquals(competitors.get(currentCompetitor), it.next());
            else
                fail("There are different a number of Competitors in the Leaderboard and the expectedOrder array");
        }
    }

    private Map<Competitor, Pair<List<MarkPassing>, List<GPSFixMovingImpl>>> competitorsAndMarkPassingsWithGpsFixes(
            Collection<CompetitorWithBoat> collection) {
        Map<Competitor, Pair<List<MarkPassing>, List<GPSFixMovingImpl>>> competitorsAndMarkPassings = new HashMap<>();
        for (Competitor competitor : collection) {
            competitorsAndMarkPassings.put(competitor,
                    new Pair<List<MarkPassing>, List<GPSFixMovingImpl>>(new ArrayList<>(), new ArrayList<>()));
        }
        return competitorsAndMarkPassings;

    }

    private void addStartMarkPassing(
            Map<Competitor, Pair<List<MarkPassing>, List<GPSFixMovingImpl>>> competitorsAndMarkPassingsWithGpsFixes,
            Iterable<String> competitorNames, TimePoint startOfRace) {
        for (String competitorName : competitorNames) {
            Competitor competitor = competitors.get(competitorName);
            competitorsAndMarkPassingsWithGpsFixes.get(competitor).getA()
                    .add(new MarkPassingImpl(startOfRace, start, competitor));
            competitorsAndMarkPassingsWithGpsFixes.get(competitor).getB()
                    .add(new GPSFixMovingImpl(new DegreePosition(0.0, 0), startOfRace,
                            new KnotSpeedWithBearingImpl(12, new DegreeBearingImpl(45))));
        }
    }

    private void addStartMarkPassing10MinutesApart(
            Map<Competitor, Pair<List<MarkPassing>, List<GPSFixMovingImpl>>> competitorsAndMarkPassingsWithGpsFixes,
            TimePoint startOfRace) {
        addStartMarkPassing(competitorsAndMarkPassingsWithGpsFixes, Arrays.asList("FastBlueBoat", "FastBlueBoat"),
                startOfRace);
        addStartMarkPassing(competitorsAndMarkPassingsWithGpsFixes, Arrays.asList("FastBlueBoat", "FastBlueBoat"),
                startOfRace.plus(Duration.ONE_MINUTE.times(10)));
    }

    private void appendGPSFixForCompetitor(
            Map<Competitor, Pair<List<MarkPassing>, List<GPSFixMovingImpl>>> competitorsAndMarkPassingsWithGpsFixes,
            String competitorName, DegreePosition position, TimePoint timePoint) {
        GPSFixMovingImpl gpsFix = new GPSFixMovingImpl(position, timePoint,
                new KnotSpeedWithBearingImpl(12, new DegreeBearingImpl(45)));
        competitorsAndMarkPassingsWithGpsFixes.get(competitors.get(competitorName)).getB().add(gpsFix);

    }

    /*
     * The fleet launched second has completely overtaken the fleet launched first. The distance between the fleets is
     * so large that the fleet that started second is in front. Within the fleets, c1 and c2 have sailed the same
     * distance, so c2 is ahead due to the ToT factor. This leads to the following ranking: SlowBlueBoat, FastBlueBoat,
     * SlowBlueBoat, FastBlueBoat
     */
    @Test
    public void testTimeOnTimeWithFactorSecondFleetOvertookFirstFleetCompetitorsInFleetHaveSameSailedDistance() {
        String[] expectedOrder = new String[] { "SlowBlueBoat", "FastBlueBoat", "SlowYellowBoat", "FastYellowBoat" };
        TimePoint startOfRace = referenceTimePoint.plus(Duration.ONE_MINUTE.times(10));
        TimePoint timePointOfViewingTheLeaderboard = startOfRace.plus(Duration.ONE_HOUR);
        setUp(c -> c.getName().contains("Fast") ? 2.0 : 1.0, c -> 0.0);
        Map<Competitor, Pair<List<MarkPassing>, List<GPSFixMovingImpl>>> competitorsAndMarkPassingsWithGpsFixes = competitorsAndMarkPassingsWithGpsFixes(
                competitors.values());
        addStartMarkPassing10MinutesApart(competitorsAndMarkPassingsWithGpsFixes, startOfRace);
        appendGPSFixForCompetitor(competitorsAndMarkPassingsWithGpsFixes, "FastYellowBoat", new DegreePosition(0.2, 0),
                timePointOfViewingTheLeaderboard);
        appendGPSFixForCompetitor(competitorsAndMarkPassingsWithGpsFixes, "SlowYellowBoat", new DegreePosition(0.2, 0),
                timePointOfViewingTheLeaderboard);
        appendGPSFixForCompetitor(competitorsAndMarkPassingsWithGpsFixes, "FastBlueBoat", new DegreePosition(0.75, 0),
                timePointOfViewingTheLeaderboard);
        appendGPSFixForCompetitor(competitorsAndMarkPassingsWithGpsFixes, "SlowBlueBoat", new DegreePosition(0.75, 0),
                timePointOfViewingTheLeaderboard);
        testOnStartLeg(expectedOrder, competitorsAndMarkPassingsWithGpsFixes, timePointOfViewingTheLeaderboard);
    }

    /*
     * The fleet launched second has completely overtaken the fleet launched first. The distance between the fleets is
     * so large that the fleet that started second is in front. Within the fleets, c2 has covered minimally less
     * distance than c1. However, due to the ToT factor, c2 is still ahead of c1. This leads to the following ranking:
     * "SlowBlueBoat", "FastBlueBoat", "SlowYellowBoat", c1Yellow
     */

    @Test
    public void testTimeOnTimeWithFactorSecondFleetOvertookFirstFleetCompetitorsInFleetHaveDifferentSailedDistancesC2Leeds() {
        String[] expectedOrder = new String[] { "SlowBlueBoat", "FastBlueBoat", "SlowYellowBoat", "FastYellowBoat" };
        TimePoint startOfRace = referenceTimePoint.plus(Duration.ONE_MINUTE.times(10));
        TimePoint timePointOfViewingTheLeaderboard = startOfRace.plus(Duration.ONE_HOUR);
        setUp(c -> c.getName().contains("Fast") ? 2.0 : 1.0, c -> 0.0);
        Map<Competitor, Pair<List<MarkPassing>, List<GPSFixMovingImpl>>> competitorsAndMarkPassingsWithGpsFixes = competitorsAndMarkPassingsWithGpsFixes(
                competitors.values());
        addStartMarkPassing10MinutesApart(competitorsAndMarkPassingsWithGpsFixes, startOfRace);
        appendGPSFixForCompetitor(competitorsAndMarkPassingsWithGpsFixes, "FastYellowBoat", new DegreePosition(0.2, 0),
                timePointOfViewingTheLeaderboard);
        appendGPSFixForCompetitor(competitorsAndMarkPassingsWithGpsFixes, "SlowYellowBoat", new DegreePosition(0.19, 0),
                timePointOfViewingTheLeaderboard);
        appendGPSFixForCompetitor(competitorsAndMarkPassingsWithGpsFixes, "FastBlueBoat", new DegreePosition(0.75, 0),
                timePointOfViewingTheLeaderboard);
        appendGPSFixForCompetitor(competitorsAndMarkPassingsWithGpsFixes, "SlowBlueBoat", new DegreePosition(0.74, 0),
                timePointOfViewingTheLeaderboard);
        testOnStartLeg(expectedOrder, competitorsAndMarkPassingsWithGpsFixes, timePointOfViewingTheLeaderboard);
    }

    /*
     * The competitor c1Blue has a large distance advantage over the other competitors. While c2Blue sails just behind
     * c1Yellow. c2Yewllow is again spatially behind c2Blue. Due to the ToT factors the following ranking results:
     * "FastBlueBoat","SlowBlueBoat", "FastYellowBoat", "SlowYellowBoat"
     */

    @Test
    public void testTimeOnTimeWithFactorSecondFleetOvertookFirstFleetCompetitorsInFleetHaveDifferentSailedDistancesC1Leads() {
        String[] expectedOrder = new String[] { "FastBlueBoat", "SlowBlueBoat", "FastYellowBoat", "SlowYellowBoat" };
        TimePoint startOfRace = referenceTimePoint.plus(Duration.ONE_MINUTE.times(10));
        TimePoint timePointOfViewingTheLeaderboard = startOfRace.plus(Duration.ONE_HOUR);
        setUp(c -> c.getName().contains("Fast") ? 2.0 : 1.0, c -> 0.0);
        Map<Competitor, Pair<List<MarkPassing>, List<GPSFixMovingImpl>>> competitorsAndMarkPassingsWithGpsFixes = competitorsAndMarkPassingsWithGpsFixes(
                competitors.values());
        addStartMarkPassing10MinutesApart(competitorsAndMarkPassingsWithGpsFixes, startOfRace);
        appendGPSFixForCompetitor(competitorsAndMarkPassingsWithGpsFixes, "FastYellowBoat", new DegreePosition(0.4, 0),
                timePointOfViewingTheLeaderboard);
        appendGPSFixForCompetitor(competitorsAndMarkPassingsWithGpsFixes, "SlowYellowBoat", new DegreePosition(0.19, 0),
                timePointOfViewingTheLeaderboard);
        appendGPSFixForCompetitor(competitorsAndMarkPassingsWithGpsFixes, "FastBlueBoat", new DegreePosition(0.75, 0),
                timePointOfViewingTheLeaderboard);
        appendGPSFixForCompetitor(competitorsAndMarkPassingsWithGpsFixes, "SlowBlueBoat", new DegreePosition(0.374, 0),
                timePointOfViewingTheLeaderboard);
        testOnStartLeg(expectedOrder, competitorsAndMarkPassingsWithGpsFixes, timePointOfViewingTheLeaderboard);
    }

    /*
     * The competitors c1Yewllow and c1Blue as well as c2Yellow and c2Blue sailed the same distance. The distance
     * between c1 and c2 is such that the ToT factors put the c2 boats in the lead, resulting in the following ranking:
     * "SlowYellowBoat", "SlowBlueBoat", "FastYellowBoat", c1Blue
     */
    @Test
    public void testTimeOnTimeWithFactorC1BlueOvertookC1YellowAndC2BlueOvertookC2Yellow() {
        String[] expectedOrder = new String[] { "FastBlueBoat", "SlowBlueBoat", "FastYellowBoat", "SlowYellowBoat" };
        TimePoint startOfRace = referenceTimePoint.plus(Duration.ONE_MINUTE.times(10));
        TimePoint timePointOfViewingTheLeaderboard = startOfRace.plus(Duration.ONE_HOUR);
        setUp(c -> c.getName().contains("Fast") ? 2.0 : 1.0, c -> 0.0);
        Map<Competitor, Pair<List<MarkPassing>, List<GPSFixMovingImpl>>> competitorsAndMarkPassingsWithGpsFixes = competitorsAndMarkPassingsWithGpsFixes(
                competitors.values());
        addStartMarkPassing10MinutesApart(competitorsAndMarkPassingsWithGpsFixes, startOfRace);
        appendGPSFixForCompetitor(competitorsAndMarkPassingsWithGpsFixes, "FastYellowBoat", new DegreePosition(0.5, 0),
                timePointOfViewingTheLeaderboard);
        appendGPSFixForCompetitor(competitorsAndMarkPassingsWithGpsFixes, "SlowYellowBoat", new DegreePosition(0.3, 0),
                timePointOfViewingTheLeaderboard);
        appendGPSFixForCompetitor(competitorsAndMarkPassingsWithGpsFixes, "FastBlueBoat", new DegreePosition(0.5, 0),
                timePointOfViewingTheLeaderboard);
        appendGPSFixForCompetitor(competitorsAndMarkPassingsWithGpsFixes, "SlowBlueBoat", new DegreePosition(0.3, 0),
                timePointOfViewingTheLeaderboard);
        testOnStartLeg(expectedOrder, competitorsAndMarkPassingsWithGpsFixes, timePointOfViewingTheLeaderboard);
    }

    /*
     * The competitors c1Yewllow and c1Blue as well as c2Yellow and c2Blue sailed the same distance. The distance
     * between c1 and c2 is such that the ToT factors put the c1 boats in the lead, resulting in the following ranking:
     * "FastYellowBoat", "FastBlueBoat", "SlowYellowBoat", "SlowBlueBoat"
     */
    @Test
    public void testTimeOnTimeWithFactor2() {
        String[] expectedOrder = new String[] { "FastYellowBoat", "SlowYellowBoat", "FastBlueBoat", "SlowBlueBoat" };
        TimePoint startOfRace = referenceTimePoint.plus(Duration.ONE_MINUTE.times(10));
        TimePoint timePointOfViewingTheLeaderboard = startOfRace.plus(Duration.ONE_HOUR);
        setUp(c -> c.getName().contains("Fast") ? 2.0 : 1.0, c -> 0.0);
        Map<Competitor, Pair<List<MarkPassing>, List<GPSFixMovingImpl>>> competitorsAndMarkPassingsWithGpsFixes = competitorsAndMarkPassingsWithGpsFixes(
                competitors.values());
        addStartMarkPassing10MinutesApart(competitorsAndMarkPassingsWithGpsFixes, startOfRace);
        appendGPSFixForCompetitor(competitorsAndMarkPassingsWithGpsFixes, "FastYellowBoat", new DegreePosition(0.5, 0),
                timePointOfViewingTheLeaderboard);
        appendGPSFixForCompetitor(competitorsAndMarkPassingsWithGpsFixes, "SlowYellowBoat", new DegreePosition(0.2, 0),
                timePointOfViewingTheLeaderboard);
        appendGPSFixForCompetitor(competitorsAndMarkPassingsWithGpsFixes, "FastBlueBoat", new DegreePosition(0.5, 0),
                timePointOfViewingTheLeaderboard);
        appendGPSFixForCompetitor(competitorsAndMarkPassingsWithGpsFixes, "SlowBlueBoat", new DegreePosition(0.2, 0),
                timePointOfViewingTheLeaderboard);
        testOnStartLeg(expectedOrder, competitorsAndMarkPassingsWithGpsFixes, timePointOfViewingTheLeaderboard);
    }

    /*
     * c1Blue has overtaken c2Yellow. However, C2Blue is still behind c2Yewllow, resulting in the following order:
     * "FastYellowBoat", "FastBlueBoat", "SlowYellowBoat", c2Blue
     */
    @Test
    public void testTimeOnTimeWithFactorC1BlueOvertookC2Yellow() {
        String[] expectedOrder = new String[] { "FastYellowBoat", "FastBlueBoat", "SlowYellowBoat", "SlowBlueBoat" };
        TimePoint startOfRace = referenceTimePoint.plus(Duration.ONE_MINUTE.times(10));
        TimePoint timePointOfViewingTheLeaderboard = startOfRace.plus(Duration.ONE_HOUR);
        setUp(c -> c.getName().contains("Fast") ? 2.0 : 1.0, c -> 0.0);
        Map<Competitor, Pair<List<MarkPassing>, List<GPSFixMovingImpl>>> competitorsAndMarkPassingsWithGpsFixes = competitorsAndMarkPassingsWithGpsFixes(
                competitors.values());
        addStartMarkPassing10MinutesApart(competitorsAndMarkPassingsWithGpsFixes, startOfRace);
        appendGPSFixForCompetitor(competitorsAndMarkPassingsWithGpsFixes, "FastYellowBoat", new DegreePosition(0.5, 0),
                timePointOfViewingTheLeaderboard);
        appendGPSFixForCompetitor(competitorsAndMarkPassingsWithGpsFixes, "SlowYellowBoat", new DegreePosition(0.2, 0),
                timePointOfViewingTheLeaderboard);
        appendGPSFixForCompetitor(competitorsAndMarkPassingsWithGpsFixes, "FastBlueBoat", new DegreePosition(0.3, 0),
                timePointOfViewingTheLeaderboard);
        appendGPSFixForCompetitor(competitorsAndMarkPassingsWithGpsFixes, "SlowBlueBoat", new DegreePosition(0.12, 0),
                timePointOfViewingTheLeaderboard);
        testOnStartLeg(expectedOrder, competitorsAndMarkPassingsWithGpsFixes, timePointOfViewingTheLeaderboard);
    }

    /*
     * c1Blue has caught up to c2Yellow enough to lead to an overtake on the leaderboard. C2Blue continues behind
     * c2Yewllow, resulting in the following order:
     */

    @Test
    public void testTimeOnTimeWithFactor3() {
        String[] expectedOrder = new String[] { "FastYellowBoat", "FastBlueBoat", "SlowYellowBoat", "SlowBlueBoat" };
        TimePoint startOfRace = referenceTimePoint.plus(Duration.ONE_MINUTE.times(10));
        TimePoint timePointOfViewingTheLeaderboard = startOfRace.plus(Duration.ONE_HOUR);
        setUp(c -> c.getName().contains("Fast") ? 2.0 : 1.0, c -> 0.0);
        Map<Competitor, Pair<List<MarkPassing>, List<GPSFixMovingImpl>>> competitorsAndMarkPassingsWithGpsFixes = competitorsAndMarkPassingsWithGpsFixes(
                competitors.values());
        addStartMarkPassing10MinutesApart(competitorsAndMarkPassingsWithGpsFixes, startOfRace);
        appendGPSFixForCompetitor(competitorsAndMarkPassingsWithGpsFixes, "FastYellowBoat", new DegreePosition(0.5, 0),
                timePointOfViewingTheLeaderboard);
        appendGPSFixForCompetitor(competitorsAndMarkPassingsWithGpsFixes, "SlowYellowBoat", new DegreePosition(0.2, 0),
                timePointOfViewingTheLeaderboard);
        appendGPSFixForCompetitor(competitorsAndMarkPassingsWithGpsFixes, "FastBlueBoat", new DegreePosition(0.19, 0),
                timePointOfViewingTheLeaderboard);
        appendGPSFixForCompetitor(competitorsAndMarkPassingsWithGpsFixes, "SlowBlueBoat", new DegreePosition(0.09, 0),
                timePointOfViewingTheLeaderboard);
        testOnStartLeg(expectedOrder, competitorsAndMarkPassingsWithGpsFixes, timePointOfViewingTheLeaderboard);
    }

    /*
     * Just a normal race without overtaking
     */
    @Test
    public void testTimeOnTimeWithFactor5() {
        String[] expectedOrder = new String[] { "FastBlueBoat", "FastYellowBoat", "SlowBlueBoat", "SlowYellowBoat" };
        TimePoint startOfRace = referenceTimePoint.plus(Duration.ONE_MINUTE.times(10));
        TimePoint timePointOfViewingTheLeaderboard = startOfRace.plus(Duration.ONE_HOUR);
        setUp(c -> c.getName().contains("Fast") ? 2.0 : 1.0, c -> 0.0);
        Map<Competitor, Pair<List<MarkPassing>, List<GPSFixMovingImpl>>> competitorsAndMarkPassingsWithGpsFixes = competitorsAndMarkPassingsWithGpsFixes(
                competitors.values());
        addStartMarkPassing10MinutesApart(competitorsAndMarkPassingsWithGpsFixes, startOfRace);
        appendGPSFixForCompetitor(competitorsAndMarkPassingsWithGpsFixes, "FastYellowBoat", new DegreePosition(0.5, 0),
                timePointOfViewingTheLeaderboard);
        appendGPSFixForCompetitor(competitorsAndMarkPassingsWithGpsFixes, "SlowYellowBoat", new DegreePosition(0.1, 0),
                timePointOfViewingTheLeaderboard);
        appendGPSFixForCompetitor(competitorsAndMarkPassingsWithGpsFixes, "FastBlueBoat", new DegreePosition(0.5, 0),
                timePointOfViewingTheLeaderboard);
        appendGPSFixForCompetitor(competitorsAndMarkPassingsWithGpsFixes, "SlowBlueBoat", new DegreePosition(0.1, 0),
                timePointOfViewingTheLeaderboard);
        testOnStartLeg(expectedOrder, competitorsAndMarkPassingsWithGpsFixes, timePointOfViewingTheLeaderboard);
    }
}
