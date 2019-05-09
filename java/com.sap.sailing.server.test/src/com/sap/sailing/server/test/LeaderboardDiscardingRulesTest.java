package com.sap.sailing.server.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.CompetitorWithBoat;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;
import com.sap.sailing.domain.leaderboard.impl.HighPoint;
import com.sap.sailing.domain.leaderboard.impl.LowPoint;
import com.sap.sailing.domain.leaderboard.impl.ThresholdBasedResultDiscardingRuleImpl;
import com.sap.sailing.domain.test.AbstractLeaderboardTest;
import com.sap.sailing.domain.test.mock.MockedTrackedRaceWithFixedRank;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.impl.RacingEventServiceImpl;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class LeaderboardDiscardingRulesTest {

    private static final String LEADERBOARDNAME = "TESTBOARD";

    private RacingEventService racingEventService;

    @Before
    public void setUp() {
        racingEventService = new RacingEventServiceImpl();
    }

    @Test
    public void testDiscardingRules() {
        racingEventService.removeLeaderboard(LEADERBOARDNAME);
        racingEventService.addFlexibleLeaderboard(LEADERBOARDNAME, null, new int[] { 1, 4 }, new LowPoint(), null);
        FlexibleLeaderboard leaderboard = (FlexibleLeaderboard) racingEventService.getLeaderboardByName(LEADERBOARDNAME);
        assertNotNull(leaderboard);
        int[] discardingRulesNew = new int[] { 1, 5 };
        leaderboard.setCrossLeaderboardResultDiscardingRule(new ThresholdBasedResultDiscardingRuleImpl(discardingRulesNew));
        racingEventService.updateStoredLeaderboard(leaderboard);
        Leaderboard leaderboardNew = racingEventService.getLeaderboardByName(LEADERBOARDNAME);
        assertNotNull(leaderboardNew);
        int[] result = ((ThresholdBasedResultDiscardingRule) leaderboardNew.getResultDiscardingRule()).getDiscardIndexResultsStartingWithHowManyRaces();
        assertArrayEquals(discardingRulesNew, result);
    }

    /**
     * See bug 892: If a competitor has two or more equally-scored races of which the to-be-discarded number
     * has to be discarded, ensure that not only one but all of them are discarded and that the sailor isn't awarded
     * more races sailed than the competition
     */
    @Test
    public void testDiscardingRulesForMultipleEquallyBadRaces() throws NoWindException {
        racingEventService.removeLeaderboard(LEADERBOARDNAME);
        racingEventService.addFlexibleLeaderboard(LEADERBOARDNAME, null, new int[] { 1, 2 }, new LowPoint(), null);
        FlexibleLeaderboard leaderboard = (FlexibleLeaderboard) racingEventService.getLeaderboardByName(LEADERBOARDNAME);
        assertNotNull(leaderboard);
        BoatClass boatClass = DomainFactory.INSTANCE.getOrCreateBoatClass("29erXX", /* typicallyStartsUpwind */ true);

        CompetitorWithBoat hasso = AbstractLeaderboardTest.createCompetitorWithBoat("Dr. Hasso Plattner");
        final TrackedRace race1 = new MockedTrackedRaceWithFixedRank(hasso, /* rank */ 123, /* started */ true, boatClass);
        leaderboard.addRace(race1, "R1", /* medalRace */false);
        final TrackedRace race2 = new MockedTrackedRaceWithFixedRank(hasso, /* rank */ 123, /* started */ true, boatClass);
        leaderboard.addRace(race2, "R2", /* medalRace */false);

        CompetitorWithBoat wolfgang = AbstractLeaderboardTest.createCompetitorWithBoat("Wolfgang Hunger");
        final TrackedRace race3 = new MockedTrackedRaceWithFixedRank(wolfgang, /* rank */ 123, /* started */ true, boatClass);
        leaderboard.addRace(race3, "R3", /* medalRace */false);
        final TrackedRace race4 = new MockedTrackedRaceWithFixedRank(wolfgang, /* rank */ 124, /* started */ true, boatClass);
        leaderboard.addRace(race4, "R4", /* medalRace */false);

        leaderboard.getScoreCorrection().correctScore(hasso, leaderboard.getRaceColumnByName("R3"), 123.);
        leaderboard.getScoreCorrection().correctScore(hasso, leaderboard.getRaceColumnByName("R4"), 123.);
        leaderboard.getScoreCorrection().correctScore(wolfgang, leaderboard.getRaceColumnByName("R1"), 122.);
        leaderboard.getScoreCorrection().correctScore(wolfgang, leaderboard.getRaceColumnByName("R2"), 122.);
        
        TimePoint now = MillisecondsTimePoint.now();
        // Hunger scores better than Plattner in this case because Hunger's two discards are 123/124, keeping 122/122
        // whereas Plattner is to discard two of the four 123 results, keeping 123/123, being two points worse than Hunger:
        double netPointsHasso = leaderboard.getNetPoints(hasso, now);
        double netPointsWolfgang = leaderboard.getNetPoints(wolfgang, now);
        assertEquals(122.+122., netPointsWolfgang, 0.0000000001);
        assertEquals(123.+123., netPointsHasso,  0.000000001);
    }

    /**
     * See bug 892: If a competitor has two or more equally-scored races of which the to-be-discarded number
     * has to be discarded, ensure that not only one but all of them are discarded and that the sailor isn't awarded
     * more races sailed than the competition
     */
    @Test
    public void testDiscardingRulesForMultipleEquallyBadRacesWithHighPointScoringScheme() throws NoWindException {
        racingEventService.removeLeaderboard(LEADERBOARDNAME);
        racingEventService.addFlexibleLeaderboard(LEADERBOARDNAME, null, new int[] { 1, 2 }, new HighPoint(), null);
        FlexibleLeaderboard leaderboard = (FlexibleLeaderboard) racingEventService.getLeaderboardByName(LEADERBOARDNAME);
        assertNotNull(leaderboard);
        BoatClass boatClass = DomainFactory.INSTANCE.getOrCreateBoatClass("ESS40", /* typicallyStartsUpwind */ true);

        CompetitorWithBoat rasmus = AbstractLeaderboardTest.createCompetitorWithBoat("Rasmus");
        final TrackedRace race1 = new MockedTrackedRaceWithFixedRank(rasmus, /* rank */ 2, /* started */ true, boatClass); // score 2.0
        leaderboard.addRace(race1, "R1", /* medalRace */false);
        final TrackedRace race2 = new MockedTrackedRaceWithFixedRank(rasmus, /* rank */ 2, /* started */ true, boatClass); // score 2.0
        leaderboard.addRace(race2, "R2", /* medalRace */false);

        CompetitorWithBoat jes = AbstractLeaderboardTest.createCompetitorWithBoat("Jes");
        final TrackedRace race3 = new MockedTrackedRaceWithFixedRank(jes, /* rank */ 2, /* started */ true, boatClass); // score 2.0
        leaderboard.addRace(race3, "R3", /* medalRace */false);
        final TrackedRace race4 = new MockedTrackedRaceWithFixedRank(jes, /* rank */ 3, /* started */ true, boatClass); // score 1.0
        leaderboard.addRace(race4, "R4", /* medalRace */false);
        
        CompetitorWithBoat nobody = AbstractLeaderboardTest.createCompetitorWithBoat("Nobody");
        final TrackedRace race5 = new MockedTrackedRaceWithFixedRank(nobody, /* rank */ 3, /* started */ true, boatClass); // score 1.0
        leaderboard.addRace(race5, "R5", /* medalRace */false);

        leaderboard.getScoreCorrection().correctScore(rasmus, leaderboard.getRaceColumnByName("R3"), 2.0);
        leaderboard.getScoreCorrection().correctScore(rasmus, leaderboard.getRaceColumnByName("R4"), 2.0);
        leaderboard.getScoreCorrection().correctScore(jes, leaderboard.getRaceColumnByName("R1"), 3.0);
        leaderboard.getScoreCorrection().correctScore(jes, leaderboard.getRaceColumnByName("R2"), 3.0);
        
        TimePoint now = MillisecondsTimePoint.now();
        // Jes scores better than Rasmus in this case because Jes's two discards are 1.0/2.0, keeping 3.0/3.0
        // whereas Rasmus is to discard two of the four 2.0 results, keeping 2.0/2.0, being two points worse than Hunger:
        double netPointsRasmus = leaderboard.getNetPoints(rasmus, now);
        double netPointsJes = leaderboard.getNetPoints(jes, now);
        assertEquals(3, Util.size(leaderboard.getCompetitors()));
        assertEquals(3.+3., netPointsJes, 0.0000000001);
        assertEquals(2.+2., netPointsRasmus,  0.000000001);
    }

    /**
     * See bug 892: If a competitor has two or more equally-scored races of which the to-be-discarded number
     * has to be discarded, ensure that not only one but all of them are discarded and that the sailor isn't awarded
     * more races sailed than the competition
     */
    @Test
    public void testDiscardingRulesForMultipleEquallyBadRacesWithNonDiscardableDisqualification() throws NoWindException {
        racingEventService.removeLeaderboard(LEADERBOARDNAME);
        racingEventService.addFlexibleLeaderboard(LEADERBOARDNAME, null, new int[] { 1, 2 }, new LowPoint(), null);
        FlexibleLeaderboard leaderboard = (FlexibleLeaderboard) racingEventService.getLeaderboardByName(LEADERBOARDNAME);
        assertNotNull(leaderboard);
        BoatClass boatClass = DomainFactory.INSTANCE.getOrCreateBoatClass("29erXX", /* typicallyStartsUpwind */ true);

        CompetitorWithBoat hasso = AbstractLeaderboardTest.createCompetitorWithBoat("Dr. Hasso Plattner");
        final TrackedRace race1 = new MockedTrackedRaceWithFixedRank(hasso, /* rank */ 123, /* started */ true, boatClass);
        leaderboard.addRace(race1, "R1", /* medalRace */false);
        final TrackedRace race2 = new MockedTrackedRaceWithFixedRank(hasso, /* rank */ 123, /* started */ true, boatClass);
        leaderboard.addRace(race2, "R2", /* medalRace */false);

        CompetitorWithBoat wolfgang = AbstractLeaderboardTest.createCompetitorWithBoat("Wolfgang Hunger");
        final TrackedRace race3 = new MockedTrackedRaceWithFixedRank(wolfgang, /* rank */ 123, /* started */ true, boatClass);
        leaderboard.addRace(race3, "R3", /* medalRace */false);
        final TrackedRace race4 = new MockedTrackedRaceWithFixedRank(wolfgang, /* rank */ 124, /* started */ true, boatClass);
        leaderboard.addRace(race4, "R4", /* medalRace */false);

        leaderboard.getScoreCorrection().correctScore(hasso, leaderboard.getRaceColumnByName("R3"), 123.);
        leaderboard.getScoreCorrection().correctScore(hasso, leaderboard.getRaceColumnByName("R4"), 123.);
        leaderboard.getScoreCorrection().setMaxPointsReason(hasso, leaderboard.getRaceColumnByName("R1"), MaxPointsReason.DNE);
        leaderboard.getScoreCorrection().correctScore(hasso, leaderboard.getRaceColumnByName("R1"), 123.);
        leaderboard.getScoreCorrection().correctScore(wolfgang, leaderboard.getRaceColumnByName("R1"), 122.);
        leaderboard.getScoreCorrection().correctScore(wolfgang, leaderboard.getRaceColumnByName("R2"), 122.);
        
        TimePoint now = MillisecondsTimePoint.now();
        // Hunger scores better than Plattner in this case because Hunger's two discards are 123/124, keeping 122/122
        // whereas Plattner is to discard two of the four 123 results, keeping 123/123, being two points worse than Hunger:
        double netPointsHasso = leaderboard.getNetPoints(hasso, now);
        double netPointsWolfgang = leaderboard.getNetPoints(wolfgang, now);
        assertEquals(122.+122., netPointsWolfgang, 0.0000000001);
        assertEquals(123.+123., netPointsHasso,  0.000000001);
    }
}
