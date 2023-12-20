package com.sap.sailing.datamining.impl.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
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
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Distance.NullDistance;
import com.sap.sse.common.impl.DegreeBearingImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class TestSegmentsTackType extends StoredTrackBasedTest {

    DynamicTrackedRaceImpl trackedRace;
    CompetitorWithBoat competitorA;

    @Before
    public void setup() {
        competitorA = createCompetitorWithBoat("A");
        trackedRace = createTestTrackedRace("TestRegatta", "TestRace", "F18", createCompetitorAndBoatsMap(competitorA),
                MillisecondsTimePoint.now(), /* useMarkPassingCalculator */ true, null,
                OneDesignRankingMetric::new);
        
        DynamicGPSFixTrack<Competitor, GPSFixMoving> competitorATrack = trackedRace.getTrack(competitorA);
        GPSFixMovingImpl currentGPS = new GPSFixMovingImpl(new DegreePosition(54.4680424, 10.234451), trackedRace.getStartOfTracking().plus(10),
                new KnotSpeedWithBearingImpl(10, new DegreeBearingImpl(45)));
        for (int i=0; i<20;i++) {
            competitorATrack.addGPSFix(currentGPS);
            Position currentPosition = currentGPS.getSpeed().travelTo(currentGPS.getPosition(), currentGPS.getTimePoint(), currentGPS.getTimePoint().plus(490));
            currentGPS = new GPSFixMovingImpl (currentPosition, currentGPS.getTimePoint().plus(240),
                    new KnotSpeedWithBearingImpl(10, new DegreeBearingImpl(45)));            
        }
        TimePoint fixTimePoint = new MillisecondsTimePoint(trackedRace.getStartOfTracking().asMillis());
        Set<MarkPassing> markPassingForCompetitor = new HashSet<MarkPassing>();
        int i=0;
        for (Waypoint waypoint : trackedRace.getRace().getCourse().getWaypoints()) {
            if (i++ <= 5) {
            markPassingForCompetitor.add(new MarkPassingImpl(fixTimePoint, waypoint, competitorA));
            fixTimePoint = new MillisecondsTimePoint(fixTimePoint.asMillis()+1000);
            }
        }
        trackedRace.updateMarkPassings(competitorA, markPassingForCompetitor);
    }

    @Test
    public void testingMissingMarkPassing() {
        // missing + skipped
        final Leaderboard leaderboard = new FlexibleLeaderboardImpl("Test",
                new ThresholdBasedResultDiscardingRuleImpl(new int[0]), new LowPoint(),
                new CourseAreaImpl("Here", UUID.randomUUID(), /* centerPosition */ null, /* radius */ null));
        final HasLeaderboardContext leaderboardContext = new LeaderboardWithContext(leaderboard, null);
        final HasTrackedRaceContext trackedRaceContext = new TrackedRaceWithContext(leaderboardContext,
                trackedRace.getTrackedRegatta().getRegatta(), null, null, trackedRace);
        final HasRaceOfCompetitorContext raceOfCompContext = new RaceOfCompetitorWithContext(trackedRaceContext, competitorA);
        final TackTypeSegmentRetrievalProcessor resultTTSegmentsRetrieval = new TackTypeSegmentRetrievalProcessor(null, Collections.emptySet(), TackTypeSegmentsDataMiningSettings.createDefaultSettings(), 0, null);
        final Iterable<HasTackTypeSegmentContext> allTTSegments = resultTTSegmentsRetrieval.retrieveData(raceOfCompContext);
        Distance sumDistance = new NullDistance();
        for (HasTackTypeSegmentContext oneTTSegment : allTTSegments) {
            if (oneTTSegment != null) {
                sumDistance = sumDistance.add(oneTTSegment.getDistance());
            }
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