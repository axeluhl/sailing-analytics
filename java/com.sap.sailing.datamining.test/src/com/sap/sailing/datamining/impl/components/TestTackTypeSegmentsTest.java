package com.sap.sailing.datamining.impl.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.sap.sailing.datamining.data.HasLeaderboardContext;
import com.sap.sailing.datamining.data.HasRaceOfCompetitorContext;
import com.sap.sailing.datamining.data.HasTackTypeSegmentContext;
import com.sap.sailing.datamining.data.HasTrackedRaceContext;
import com.sap.sailing.datamining.impl.data.LeaderboardWithContext;
import com.sap.sailing.datamining.impl.data.RaceOfCompetitorWithContext;
import com.sap.sailing.datamining.impl.data.TrackedRaceWithContext;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorWithBoat;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.Sideline;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.BoatImpl;
import com.sap.sailing.domain.base.impl.CompetitorWithBoatImpl;
import com.sap.sailing.domain.base.impl.ControlPointWithTwoMarksImpl;
import com.sap.sailing.domain.base.impl.CourseAreaImpl;
import com.sap.sailing.domain.base.impl.CourseImpl;
import com.sap.sailing.domain.base.impl.DynamicBoat;
import com.sap.sailing.domain.base.impl.DynamicCompetitorWithBoat;
import com.sap.sailing.domain.base.impl.MarkImpl;
import com.sap.sailing.domain.base.impl.NationalityImpl;
import com.sap.sailing.domain.base.impl.PersonImpl;
import com.sap.sailing.domain.base.impl.RaceDefinitionImpl;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sailing.domain.base.impl.WaypointImpl;
import com.sap.sailing.domain.common.CompetitorRegistrationType;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.WindImpl;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.common.tracking.impl.GPSFixImpl;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.impl.FlexibleLeaderboardImpl;
import com.sap.sailing.domain.leaderboard.impl.LowPoint;
import com.sap.sailing.domain.leaderboard.impl.ThresholdBasedResultDiscardingRuleImpl;
import com.sap.sailing.domain.racelog.RaceLogAndTrackedRaceResolver;
import com.sap.sailing.domain.racelog.impl.EmptyRaceLogStore;
import com.sap.sailing.domain.ranking.OneDesignRankingMetric;
import com.sap.sailing.domain.ranking.RankingMetricConstructor;
import com.sap.sailing.domain.regattalog.impl.EmptyRegattaLogStore;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRaceImpl;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRegattaImpl;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sse.common.Color;
import com.sap.sse.common.Distance;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.DegreeBearingImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class TestTackTypeSegmentsTest {

    public static DynamicCompetitorWithBoat createCompetitorWithBoat(String competitorName) {
        DynamicBoat boat = (DynamicBoat) new BoatImpl("id12345", competitorName + "'s boat",
                new BoatClassImpl("505", /* typicallyStartsUpwind */ true), /* sailID */ null);
        return new CompetitorWithBoatImpl(competitorName, competitorName, "KYC", Color.RED, null, null,
                new TeamImpl("STG",
                        Collections.singleton(new PersonImpl(competitorName, new NationalityImpl("GER"),
                                /* dateOfBirth */ null, "This is famous " + competitorName)),
                        new PersonImpl("Rigo van Maas", new NationalityImpl("NED"), /* dateOfBirth */null,
                                "This is Rigo, the coach")),
                /* timeOnTimeFactor */ null, /* timeOnDistanceAllowancePerNauticalMile */ null, /* searchTag */ null,
                boat);
    }

    public static DynamicTrackedRaceImpl createTestTrackedRace(String regattaName, String raceName,
            String boatClassName, Map<Competitor, Boat> competitorsAndBoats, TimePoint timePointForFixes,
            boolean useMarkPassingCalculator, RaceLogAndTrackedRaceResolver raceLogResolver,
            RankingMetricConstructor rankingMetricConstructor) {
        BoatClassImpl boatClass = new BoatClassImpl(boatClassName, /* typicallyStartsUpwind */ true);
        Regatta regatta = new RegattaImpl(EmptyRaceLogStore.INSTANCE, EmptyRegattaLogStore.INSTANCE,
                RegattaImpl.getDefaultName(regattaName, boatClass.getName()), boatClass,
                /* canBoatsOfCompetitorsChangePerRace */ true, CompetitorRegistrationType.CLOSED, /* startDate */ null,
                /* endDate */ null, /* trackedRegattaRegistry */ null,
                DomainFactory.INSTANCE.createScoringScheme(ScoringSchemeType.LOW_POINT), "123", null,
                /* registrationLinkSecret */ UUID.randomUUID().toString());
        TrackedRegatta trackedRegatta = new DynamicTrackedRegattaImpl(regatta);
        List<Waypoint> waypoints = new ArrayList<Waypoint>();
        // create a two-lap upwind/downwind course:
        MarkImpl left = new MarkImpl("Left lee gate buoy");
        MarkImpl right = new MarkImpl("Right lee gate buoy");
        ControlPoint leeGate = new ControlPointWithTwoMarksImpl(left, right, "Lee Gate", "Lee Gate");
        Mark windwardMark = new MarkImpl("Windward mark");
        waypoints.add(new WaypointImpl(leeGate));
        waypoints.add(new WaypointImpl(windwardMark));
        waypoints.add(new WaypointImpl(leeGate));
        waypoints.add(new WaypointImpl(windwardMark));
        waypoints.add(new WaypointImpl(leeGate));
        Course course = new CourseImpl(raceName, waypoints);
        RaceDefinition race = new RaceDefinitionImpl(raceName, course, boatClass, competitorsAndBoats);
        regatta.addRace(race);
        DynamicTrackedRaceImpl trackedRace = new DynamicTrackedRaceImpl(trackedRegatta, race,
                Collections.<Sideline> emptyList(), EmptyWindStore.INSTANCE, /* delayToLiveInMillis */ 0,
                /* millisecondsOverWhichToAverageWind */ 30000, /* millisecondsOverWhichToAverageSpeed */ 30000,
                /* delay for wind estimation cache invalidation */ 0, useMarkPassingCalculator,
                rankingMetricConstructor, raceLogResolver, /* trackingConnectorInfo */ null,
                /* markPassingRaceFingerprintRegistry */ null);
        // in this simplified artificial course, the top mark is exactly north of the right leeward gate
        DegreePosition topPosition = new DegreePosition(54.48, 10.24);
        TimePoint afterTheRace = new MillisecondsTimePoint(timePointForFixes.asMillis() + 36000000); // 10h after the
                                                                                                     // fix timed
        trackedRace.setStartOfTrackingReceived(timePointForFixes);
        trackedRace.getOrCreateTrack(left)
                .addGPSFix(new GPSFixImpl(new DegreePosition(54.4680424, 10.234451), new MillisecondsTimePoint(0)));
        trackedRace.getOrCreateTrack(right)
                .addGPSFix(new GPSFixImpl(new DegreePosition(54.4680424, 10.24), new MillisecondsTimePoint(0)));
        trackedRace.getOrCreateTrack(windwardMark).addGPSFix(new GPSFixImpl(topPosition, new MillisecondsTimePoint(0)));
        trackedRace.getOrCreateTrack(left)
                .addGPSFix(new GPSFixImpl(new DegreePosition(54.4680424, 10.234451), afterTheRace));
        trackedRace.getOrCreateTrack(right)
                .addGPSFix(new GPSFixImpl(new DegreePosition(54.4680424, 10.24), afterTheRace));
        trackedRace.getOrCreateTrack(windwardMark).addGPSFix(new GPSFixImpl(topPosition, afterTheRace));
        trackedRace.recordWind(
                new WindImpl(topPosition, timePointForFixes,
                        new KnotSpeedWithBearingImpl(/* speedInKnots */14.7, new DegreeBearingImpl(180))),
                new WindSourceImpl(WindSourceType.WEB));
        return trackedRace;
    }

    DynamicTrackedRaceImpl trackedRace;
    DynamicTrackedRaceImpl trackedRace2;
    CompetitorWithBoat competitor1;
    CompetitorWithBoat competitor2;

    @Before
    public void setup() {

        // RENNEN 1
        competitor1 = createCompetitorWithBoat("C1");
        competitor2 = createCompetitorWithBoat("C2");
        Map<Competitor, Boat> competitorsAndBoats2 = new LinkedHashMap<>();
        competitorsAndBoats2.put(competitor1, competitor1.getBoat());
        competitorsAndBoats2.put(competitor2, competitor2.getBoat());
        
        trackedRace2 = createTestTrackedRace("TestRegatta", "TestRace", "F18", competitorsAndBoats2,
                MillisecondsTimePoint.now(), /* useMarkPassingCalculator */ false, null,
                OneDesignRankingMetric::new);

        // RENNEN 2
        List<Waypoint> waypoints = new ArrayList<Waypoint>();
        final WaypointImpl wp1 = new WaypointImpl(new MarkImpl("Test Mark 1"));
        waypoints.add(wp1);
        final WaypointImpl wp2 = new WaypointImpl(new MarkImpl("Test Mark 2"));
        waypoints.add(wp2);
        final WaypointImpl wp3 = new WaypointImpl(new MarkImpl("Test Mark 3"));
        waypoints.add(wp3);

        CourseImpl course = new CourseImpl("Test Course", waypoints);
        final BoatClass boatClass = new BoatClassImpl("505", /* upwind start */true);
        final CompetitorWithBoat hasso = createCompetitorWithBoat("Hasso");
        final Map<Competitor, Boat> competitorsAndBoats = new HashMap<>();
        competitorsAndBoats.put(hasso, hasso.getBoat());
        trackedRace = new DynamicTrackedRaceImpl(
                /* trackedRegatta */new DynamicTrackedRegattaImpl(new RegattaImpl("test", null, true,
                        CompetitorRegistrationType.CLOSED, null, null, new HashSet<Series>(), false, null, "test", null,
                        OneDesignRankingMetric::new, /* registrationLinkSecret */ UUID.randomUUID().toString())),
                new RaceDefinitionImpl("Test Race", course, boatClass, competitorsAndBoats),
                Collections.<Sideline> emptyList(), EmptyWindStore.INSTANCE, /* delayToLiveInMillis */3000,
                /* millisecondsOverWhichToAverageWind */30000, /* millisecondsOverWhichToAverageSpeed */8000,
                /* useMarkPassingCalculator */ false, OneDesignRankingMetric::new,
                mock(RaceLogAndTrackedRaceResolver.class), /* trackingConnectorInfo */ null,
                /* markPassingRaceFingerprintRegistry */ null);
    }

    @Test
    public void testingMissingMarkPassing() {
        // missing + skipped
        Leaderboard leaderboard = new FlexibleLeaderboardImpl("Test",
                new ThresholdBasedResultDiscardingRuleImpl(new int[0]), new LowPoint(),
                new CourseAreaImpl("Here", UUID.randomUUID(), /* centerPosition */ null, /* radius */ null));
        final HasLeaderboardContext leaderboardContext = new LeaderboardWithContext(leaderboard, null);
        HasTrackedRaceContext trackedRaceContext = new TrackedRaceWithContext(leaderboardContext,
                trackedRace2.getTrackedRegatta().getRegatta(), null, null, trackedRace2);
        HasRaceOfCompetitorContext raceOfCompContext = new RaceOfCompetitorWithContext(trackedRaceContext, competitor1);

        // Iterable<HasTackTypeSegmentContext> result = getInstancesOfType(compkeineAhnung,
        // HasTackTypeSegmentContext.class);
        //HasTackTypeSegmentContext resultTTSegments = (HasTackTypeSegmentContext) raceOfCompContext;
        
        final TackTypeSegmentRetrievalProcessor resultTTSegmentsRetrieval = Mockito.mock(TackTypeSegmentRetrievalProcessor.class);

        Iterable<HasTackTypeSegmentContext> allTTSegments = resultTTSegmentsRetrieval.retrieveData(raceOfCompContext);
        Distance sumDistance = null;
        for (HasTackTypeSegmentContext oneTTSegment : allTTSegments) {
            sumDistance = sumDistance.add(oneTTSegment.getDistance());
        }
        assertTrue(sumDistance == null);
        assertEquals(null, sumDistance);
    }

    @Test
    public void testingFixExactOnMarkPassing() {
        
    }

    @Test
    public void testingOpenEndedRace() {
    }

    @Test
    public void testingFinishedRace() {
    }

}
