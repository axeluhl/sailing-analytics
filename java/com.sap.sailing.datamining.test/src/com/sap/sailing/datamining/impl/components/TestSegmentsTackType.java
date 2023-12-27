package com.sap.sailing.datamining.impl.components;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.datamining.data.HasLeaderboardContext;
import com.sap.sailing.datamining.data.HasRaceOfCompetitorContext;
import com.sap.sailing.datamining.data.HasTackTypeSegmentContext;
import com.sap.sailing.datamining.data.HasTrackedRaceContext;
import com.sap.sailing.datamining.impl.data.LeaderboardWithContext;
import com.sap.sailing.datamining.impl.data.RaceOfCompetitorWithContext;
import com.sap.sailing.datamining.impl.data.TrackedRaceWithContext;
import com.sap.sailing.datamining.shared.TackTypeSegmentsDataMiningSettings;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorWithBoat;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.CourseAreaImpl;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.common.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.impl.FlexibleLeaderboardImpl;
import com.sap.sailing.domain.leaderboard.impl.LowPoint;
import com.sap.sailing.domain.leaderboard.impl.ThresholdBasedResultDiscardingRuleImpl;
import com.sap.sailing.domain.ranking.OneDesignRankingMetric;
import com.sap.sailing.domain.test.StoredTrackBasedTest;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRaceImpl;
import com.sap.sailing.domain.tracking.impl.MarkPassingImpl;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Distance.NullDistance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.DegreeBearingImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class TestSegmentsTackType extends StoredTrackBasedTest {

    private DynamicTrackedRaceImpl trackedRace;
    private CompetitorWithBoat competitorA;
    private HasRaceOfCompetitorContext raceOfCompContext;
    private TackTypeSegmentRetrievalProcessor resultTTSegmentsRetrieval;

    @Before
    public void setup() {
        competitorA = createCompetitorWithBoat("A");
        trackedRace = createTestTrackedRace("TestRegatta", "TestRace", "F18", createCompetitorAndBoatsMap(competitorA),
                MillisecondsTimePoint.now(), /* useMarkPassingCalculator */ true, null,
                OneDesignRankingMetric::new);
        final Leaderboard leaderboard = new FlexibleLeaderboardImpl("Test",
                new ThresholdBasedResultDiscardingRuleImpl(new int[0]), new LowPoint(),
                new CourseAreaImpl("Here", UUID.randomUUID(), /* centerPosition */ null, /* radius */ null));
        final HasLeaderboardContext leaderboardContext = new LeaderboardWithContext(leaderboard, null);
        final HasTrackedRaceContext trackedRaceContext = new TrackedRaceWithContext(leaderboardContext,
                trackedRace.getTrackedRegatta().getRegatta(), null, null, trackedRace);
        raceOfCompContext = new RaceOfCompetitorWithContext(trackedRaceContext, competitorA);
        resultTTSegmentsRetrieval = new TackTypeSegmentRetrievalProcessor(null, Collections.emptySet(), TackTypeSegmentsDataMiningSettings.createDefaultSettings(), 0, null);
    }
    
    private Iterable<HasTackTypeSegmentContext> retrieveData() {
        return resultTTSegmentsRetrieval.retrieveData(raceOfCompContext);
    }

    @Test
    public void testingSegmentsAreNotNull() {
        // set up GPS fixes for competitor, as well as mark passings:
        DynamicGPSFixTrack<Competitor, GPSFixMoving> competitorATrack = trackedRace.getTrack(competitorA);
        final KnotSpeedWithBearingImpl sogCog = new KnotSpeedWithBearingImpl(10, new DegreeBearingImpl(45));
        TimePoint timePoint = trackedRace.getStartOfTracking().plus(10);
        GPSFixMovingImpl currentGPS = new GPSFixMovingImpl(new DegreePosition(54.4680424, 10.234451), timePoint, sogCog);
        final Duration timeBetweenFixes = Duration.ofMillis(490);
        for (int i=0; i<20; i++) {
            competitorATrack.addGPSFix(currentGPS);
            final Position currentPosition = sogCog.travelTo(currentGPS.getPosition(), timeBetweenFixes);
            timePoint = timePoint.plus(timeBetweenFixes);
            currentGPS = new GPSFixMovingImpl(currentPosition, timePoint, sogCog);
        }
        TimePoint markPassingTimePoint = trackedRace.getStartOfTracking().plus(20);
        final List<MarkPassing> markPassingsForCompetitor = new ArrayList<>();
        final Duration legDuration = Duration.ofSeconds(60);
        for (Waypoint waypoint : trackedRace.getRace().getCourse().getWaypoints()) {
            markPassingsForCompetitor.add(new MarkPassingImpl(markPassingTimePoint, waypoint, competitorA));
            markPassingTimePoint = markPassingTimePoint.plus(legDuration);
        }
        trackedRace.updateMarkPassings(competitorA, markPassingsForCompetitor);
        // now run the actual test:
        Iterable<HasTackTypeSegmentContext> allTTSegments = retrieveData();
        Distance sumDistance = new NullDistance();
        for (HasTackTypeSegmentContext oneTTSegment : allTTSegments) {
            if (oneTTSegment != null) {
                sumDistance = sumDistance.add(oneTTSegment.getDistance());
            }
        }
        assertTrue(sumDistance.compareTo(Distance.NULL) > 0);
    }

    @Test
    public void testingMissingMarkPassing() {
        // missing + skipped
    }

    @Test
    public void testingFixExactlyOnMarkPassing() {
    }

    @Test
    public void testingOpenEndedRace() {
    }

    @Test
    public void testingFinishedRace() {
    }
}