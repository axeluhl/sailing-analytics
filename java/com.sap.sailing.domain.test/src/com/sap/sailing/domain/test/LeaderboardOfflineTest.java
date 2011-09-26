package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.BoatImpl;
import com.sap.sailing.domain.base.impl.CompetitorImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.base.impl.NationalityImpl;
import com.sap.sailing.domain.base.impl.PersonImpl;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.RaceInLeaderboard;
import com.sap.sailing.domain.leaderboard.ScoreCorrection.MaxPointsReason;
import com.sap.sailing.domain.leaderboard.impl.LeaderboardImpl;
import com.sap.sailing.domain.leaderboard.impl.ResultDiscardingRuleImpl;
import com.sap.sailing.domain.leaderboard.impl.ScoreCorrectionImpl;
import com.sap.sailing.domain.tracking.NoWindException;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.util.Util;
import com.sap.sailing.util.Util.Pair;

public class LeaderboardOfflineTest {
    private Set<TrackedRace> testRaces;
    private Map<TrackedRace, RaceInLeaderboard> raceColumnsInLeaderboard;
    private Competitor competitor;
    
    @Before
    public void setUp() {
        competitor = createCompetitor("Wolfgang Hunger");
    }
    
    private CompetitorImpl createCompetitor(String competitorName) {
        return new CompetitorImpl(123, competitorName, new TeamImpl("STG", Collections.singleton(
                new PersonImpl(competitorName, new NationalityImpl("Germany", "GER"),
                /* dateOfBirth */ null, "This is famous "+competitorName)),
                new PersonImpl("Rigo van Maas", new NationalityImpl("The Netherlands", "NED"),
                /* dateOfBirth */null, "This is Rigo, the coach")), new BoatImpl(competitorName + "'s boat",
                new BoatClassImpl("505"), null));
    }

    public void setupRaces(int numberOfStartedRaces, int numberOfNotStartedRaces) {
        testRaces = new HashSet<TrackedRace>();
        raceColumnsInLeaderboard = new HashMap<TrackedRace, RaceInLeaderboard>();
        for (int i=0; i<numberOfStartedRaces; i++) {
            TrackedRace r = new MockedTrackedRaceWithFixedRank(competitor, i+1, /* started */ true);
            testRaces.add(r); // hash set should take care of more or less randomly permuting the races
        }
        for (int i=0; i<numberOfNotStartedRaces; i++) {
            TrackedRace r = new MockedTrackedRaceWithFixedRank(competitor, -1, /* started */ false);
            testRaces.add(r); // hash set should take care of more or less randomly permuting the races
        }
    }
    
    @Test
    public void testSetup() {
        setupRaces(3, 7);
        assertEquals(10, testRaces.size());
    }
    
    @Test
    public void simpleLeaderboardTest() throws NoWindException {
        for (int numberOfUntrackedRaces : new int[] { 0, 1, 2, 3 }) {
            for (boolean addOneMedalRace : new boolean[] { false, true }) {
                for (Integer carry : new Integer[] { null, 0, 1, 2, 3 }) {
                    testLeaderboard(0, 9, 3, 6, carry, addOneMedalRace, numberOfUntrackedRaces);
                    testLeaderboard(2, 9, 3, 6, carry, addOneMedalRace, numberOfUntrackedRaces);
                    testLeaderboard(3, 9, 3, 6, carry, addOneMedalRace, numberOfUntrackedRaces);
                    testLeaderboard(4, 9, 3, 6, carry, addOneMedalRace, numberOfUntrackedRaces);
                    testLeaderboard(6, 9, 3, 6, carry, addOneMedalRace, numberOfUntrackedRaces);
                    testLeaderboard(7, 9, 3, 6, carry, addOneMedalRace, numberOfUntrackedRaces);
                    testLeaderboard(8, 9, 3, 6, carry, addOneMedalRace, numberOfUntrackedRaces);
                    testLeaderboard(0, 0, 3, 6, carry, addOneMedalRace, numberOfUntrackedRaces);
                    testLeaderboard(2, 0, 3, 6, carry, addOneMedalRace, numberOfUntrackedRaces);
                    testLeaderboard(3, 0, 3, 6, carry, addOneMedalRace, numberOfUntrackedRaces);
                    testLeaderboard(4, 0, 3, 6, carry, addOneMedalRace, numberOfUntrackedRaces);
                    testLeaderboard(6, 0, 3, 6, carry, addOneMedalRace, numberOfUntrackedRaces);
                    testLeaderboard(7, 0, 3, 6, carry, addOneMedalRace, numberOfUntrackedRaces);
                    testLeaderboard(8, 0, 3, 6, carry, addOneMedalRace, numberOfUntrackedRaces);
                }
            }
        }
    }
    
    @Test
    public void ensureMedalRaceParamIsIgnoredIfRaceColumnAlreadyExists() {
        Leaderboard leaderboard = new LeaderboardImpl("Test Leaderboard", new ScoreCorrectionImpl(), new ResultDiscardingRuleImpl(
                new int[] { 5, 8 }));
        final String columnName = "abc";
        setupRaces(1, 0);
        leaderboard.addRaceColumn(columnName, /* medalRace */ true);
        leaderboard.addRace(testRaces.iterator().next(), columnName, /* medalRace */ false);
        assertTrue(leaderboard.getRaceColumnByName(columnName).isMedalRace());
    }

    @Test
    public void testMaxPointsDiscard() throws NoWindException {
        testRaces = new HashSet<TrackedRace>();
        raceColumnsInLeaderboard = new HashMap<TrackedRace, RaceInLeaderboard>();
        Competitor c2 = createCompetitor("Marcus Baur");
        Competitor c3 = createCompetitor("Robert Stanjek");
        for (int i=0; i<3; i++) {
            MockedTrackedRaceWithFixedRankAndManyCompetitors r = new MockedTrackedRaceWithFixedRankAndManyCompetitors(competitor, i+1, /* started */ true);
            r.addCompetitor(c2);
            r.addCompetitor(c3); // this makes maxPoints==4
            testRaces.add(r); // hash set should take care of more or less randomly permuting the races
        }
        ScoreCorrectionImpl scoreCorrection = new ScoreCorrectionImpl();
        Leaderboard leaderboard = new LeaderboardImpl("Test Leaderboard", scoreCorrection, new ResultDiscardingRuleImpl(
                new int[] { 1 }));
        int i=0;
        int bestScore = Integer.MAX_VALUE;
        RaceInLeaderboard bestScoringRaceColumn = null;
        for (TrackedRace race : testRaces) {
            i++;
            RaceInLeaderboard raceColumn = leaderboard.addRace(race, "Test Race " + i, /* medalRace */ false);
            raceColumnsInLeaderboard.put(race, raceColumn);
            if (race.getRank(competitor) < bestScore) {
                bestScore = race.getRank(competitor);
                bestScoringRaceColumn = raceColumn;
            }
        }
        scoreCorrection.setMaxPointsReason(competitor, bestScoringRaceColumn, MaxPointsReason.DSQ);
        // assert that best scoring but now disqualified race gets max net points because of disqualification:
        assertEquals(Util.size(bestScoringRaceColumn.getTrackedRace().getRace().getCompetitors())+1,
                leaderboard.getEntry(competitor, bestScoringRaceColumn, MillisecondsTimePoint.now()).getNetPoints());
        // now assert that it gets discarded because due to disqualification it scores worse than all others:
        assertEquals(0, leaderboard.getEntry(competitor, bestScoringRaceColumn, MillisecondsTimePoint.now()).getTotalPoints());
    }
    
    protected void testLeaderboard(int numberOfStartedRaces, int numberOfNotStartedRaces, int firstDiscardingThreshold,
            int secondDiscardingThreshold, Integer carry, boolean addOneMedalRace, int numberOfUntrackedRaces) throws NoWindException {
        setupRaces(numberOfStartedRaces, numberOfNotStartedRaces);
        Leaderboard leaderboard = new LeaderboardImpl("Test Leaderboard", new ScoreCorrectionImpl(), new ResultDiscardingRuleImpl(
                new int[] { firstDiscardingThreshold, secondDiscardingThreshold }));
        int i=0;
        for (TrackedRace race : testRaces) {
            i++;
            raceColumnsInLeaderboard.put(race, leaderboard.addRace(race, "Test Race "+i,
                    /* medalRace */ numberOfUntrackedRaces == 0 && addOneMedalRace && i == testRaces.size()));
        }
        // add a few race columns not yet connected to a tracked race
        for (int j=0; j<numberOfUntrackedRaces; j++) {
            i++;
            leaderboard.addRaceColumn("Test Race "+i, /* medalRace */ addOneMedalRace && j == numberOfUntrackedRaces-1);
        }
        if (carry != null) {
            leaderboard.setCarriedPoints(competitor, carry);
        }
        TimePoint now = MillisecondsTimePoint.now();
        int carryInt = (carry == null ? 0 : carry);
        int totalPoints = carryInt;
        int medalRacePoints = getMedalRacePoints(competitor, now);
        for (TrackedRace race : testRaces) {
            RaceInLeaderboard raceColumn = raceColumnsInLeaderboard.get(race);
            Pair<Competitor, RaceInLeaderboard> key = new Pair<Competitor, RaceInLeaderboard>(competitor, raceColumn);
            if (race.hasStarted(now)) {
                int rank = race.getRank(competitor, now);
                assertEquals(rank, leaderboard.getTrackedPoints(competitor, raceColumn, now));
                assertEquals(rank, leaderboard.getContent(now).get(key).getTrackedPoints());
                assertEquals(rank, leaderboard.getEntry(competitor, raceColumn, now).getTrackedPoints());
                assertEquals(rank, leaderboard.getNetPoints(competitor, raceColumn, now));
                assertEquals(rank, leaderboard.getContent(now).get(key).getNetPoints());
                assertEquals(rank, leaderboard.getEntry(competitor, raceColumn, now).getNetPoints());
                // One race is discarded because four races were started, and for [3-6) one race can be discarded.
                // The discarded race is the worst of those started, so the one with rank 4.
                int expectedNumberOfDiscardedRaces =
                        numberOfStartedRaces < firstDiscardingThreshold ? 0 : numberOfStartedRaces < secondDiscardingThreshold ? 1 : 2;
                int expected = !raceColumn.isMedalRace()
                        // FIXME with the medal race scoring worse than one of the races to discard, rank cannot simply be compared to numberOfStartedRaces-expectedNumberOfDiscardedRaces
                        && rank > numberOfStartedRaces - expectedNumberOfDiscardedRaces
                                - (addOneMedalRace && numberOfStartedRaces-medalRacePoints<expectedNumberOfDiscardedRaces ? 1 : 0) ? 0 :
                                    rank==medalRacePoints?2*rank:rank;
                assertEquals(expected, leaderboard.getTotalPoints(competitor, raceColumn, now));
                assertEquals(expected, leaderboard.getContent(now).get(key).getTotalPoints());
                assertEquals(expected, leaderboard.getEntry(competitor, raceColumn, now).getTotalPoints());
                totalPoints += leaderboard.getContent(now).get(key).getTotalPoints();
            } else {
                assertEquals(0, leaderboard.getTrackedPoints(competitor, raceColumn, now));
                assertEquals(0, leaderboard.getNetPoints(competitor, raceColumn, now));
                assertEquals(0, leaderboard.getContent(now).get(key).getTrackedPoints());
                assertEquals(0, leaderboard.getContent(now).get(key).getNetPoints());
                assertEquals(0, leaderboard.getEntry(competitor, raceColumn, now).getTrackedPoints());
                assertEquals(0, leaderboard.getEntry(competitor, raceColumn, now).getNetPoints());
                // no increment on totalPoints
            }
        }
        assertEquals(totalPoints, leaderboard.getTotalPoints(competitor, now));
    }
    
    private int getMedalRacePoints(Competitor competitor, TimePoint at) throws NoWindException {
        for (TrackedRace r : testRaces) {
            if (raceColumnsInLeaderboard.get(r) != null && raceColumnsInLeaderboard.get(r).isMedalRace() &&
                    raceColumnsInLeaderboard.get(r).getTrackedRace().hasStarted(at)) {
                return raceColumnsInLeaderboard.get(r).getTrackedRace().getRank(competitor, at);
            }
        }
        return 0;
    }
}
