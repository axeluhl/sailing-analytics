package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.BoatImpl;
import com.sap.sailing.domain.base.impl.CompetitorImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.base.impl.NationalityImpl;
import com.sap.sailing.domain.base.impl.PersonImpl;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.impl.LeaderboardImpl;
import com.sap.sailing.domain.leaderboard.impl.ResultDiscardingRuleImpl;
import com.sap.sailing.domain.leaderboard.impl.ScoreCorrectionImpl;
import com.sap.sailing.domain.test.mock.MockedTrackedRace;
import com.sap.sailing.domain.tracking.NoWindException;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.util.Util.Pair;

public class LeaderboardOfflineTest {
    private Set<TrackedRace> testRaces;
    private Competitor competitor;
    
    @Before
    public void setUp() {
        competitor = new CompetitorImpl(123, "Wolfgang Hunger", new TeamImpl("STG", Collections.singleton(
                new PersonImpl("Wolfgang Hunger", new NationalityImpl("Germany", "GER"),
                /* dateOfBirth */ null, "This is famous Wolfgang Hunger")), new PersonImpl("Rigo van Maas", new NationalityImpl("The Netherlands", "NED"),
                        /* dateOfBirth */ null, "This is Rigo, the coach")), new BoatImpl("Wolfgang Hunger's boat", new BoatClassImpl("505")));
    }
    
    public void setupRaces(int numberOfStartedRaces, int numberOfNotStartedRaces) {
        testRaces = new HashSet<TrackedRace>();
        for (int i=0; i<numberOfStartedRaces; i++) {
            TrackedRace r = new MockedTrackedRaceWithFixedRank(i+1, /* started */ true);
            testRaces.add(r); // hash set should take care of more or less randomly permuting the races
        }
        for (int i=0; i<numberOfNotStartedRaces; i++) {
            TrackedRace r = new MockedTrackedRaceWithFixedRank(-1, /* started */ false);
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
        for (Integer carry : new Integer[] { null, 0, 1, 2, 3 }) {
            testLeaderboard(0, 9, 3, 6, carry);
            testLeaderboard(2, 9, 3, 6, carry);
            testLeaderboard(3, 9, 3, 6, carry);
            testLeaderboard(4, 9, 3, 6, carry);
            testLeaderboard(6, 9, 3, 6, carry);
            testLeaderboard(7, 9, 3, 6, carry);
            testLeaderboard(8, 9, 3, 6, carry);
        }
    }

    protected void testLeaderboard(int numberOfStartedRaces, int numberOfNotStartedRaces, int firstDiscardingThreshold,
            int secondDiscardingThreshold, Integer carry) throws NoWindException {
        setupRaces(numberOfStartedRaces, numberOfNotStartedRaces);
        Leaderboard leaderboard = new LeaderboardImpl("Test Leaderboard", new ScoreCorrectionImpl(), new ResultDiscardingRuleImpl(
                new int[] { firstDiscardingThreshold, secondDiscardingThreshold }));
        int i=0;
        for (TrackedRace race : testRaces) {
            leaderboard.addRace(race, "Test Race "+(++i));
        }
        // add a few race columns not yet connected to a tracked race
        for (int j=0; j<3; j++) {
            leaderboard.addRaceColumn("Test Race "+(++i));
        }
        if (carry != null) {
            leaderboard.setCarriedPoints(competitor, carry);
        }
        TimePoint now = MillisecondsTimePoint.now();
        int carryInt = (carry == null ? 0 : carry);
        int totalPoints = carryInt;
        for (TrackedRace race : testRaces) {
            Pair<Competitor, TrackedRace> key = new Pair<Competitor, TrackedRace>(competitor, race);
            if (race.hasStarted(now)) {
                int rank = race.getRank(competitor, now);
                assertEquals(rank, leaderboard.getTrackedPoints(competitor, race, now));
                assertEquals(rank, leaderboard.getContent(now).get(key).getTrackedPoints());
                assertEquals(rank, leaderboard.getEntry(competitor, race, now).getTrackedPoints());
                assertEquals(rank, leaderboard.getNetPoints(competitor, race, now));
                assertEquals(rank, leaderboard.getContent(now).get(key).getNetPoints());
                assertEquals(rank, leaderboard.getEntry(competitor, race, now).getNetPoints());
                // One race is discarded because four races were started, and for [3-6) there can be one race discarded.
                // The discarded race is the worst from those started, so the one with rank 4.
                int expectedNumberOfDiscardedRaces =
                        numberOfStartedRaces < firstDiscardingThreshold ? 0 : numberOfStartedRaces < secondDiscardingThreshold ? 1 : 2;
                assertEquals(rank > numberOfStartedRaces - expectedNumberOfDiscardedRaces ? 0 : rank,
                        leaderboard.getTotalPoints(competitor, race, now));
                assertEquals(rank > numberOfStartedRaces - expectedNumberOfDiscardedRaces ? 0 : rank,
                        leaderboard.getContent(now).get(key).getTotalPoints());
                assertEquals(rank > numberOfStartedRaces - expectedNumberOfDiscardedRaces ? 0 : rank,
                        leaderboard.getEntry(competitor, race, now).getTotalPoints());
                totalPoints += leaderboard.getContent(now).get(key).getTotalPoints();
            } else {
                assertEquals(0, leaderboard.getTrackedPoints(competitor, race, now));
                assertEquals(0, leaderboard.getNetPoints(competitor, race, now));
                assertEquals(0, leaderboard.getContent(now).get(key).getTrackedPoints());
                assertEquals(0, leaderboard.getContent(now).get(key).getNetPoints());
                assertEquals(0, leaderboard.getEntry(competitor, race, now).getTrackedPoints());
                assertEquals(0, leaderboard.getEntry(competitor, race, now).getNetPoints());
                // no increment on totalPoints
            }
        }
        assertEquals(totalPoints, leaderboard.getTotalPoints(competitor, now));
    }
    
    private class MockedTrackedRaceWithFixedRank extends MockedTrackedRace {
        private final int rank;
        private final boolean started;
        private final RaceDefinition raceDefinition;
        
        public MockedTrackedRaceWithFixedRank(int rank, boolean started) {
            this.rank = rank;
            this.started = started;
            this.raceDefinition = new RaceDefinition() {
                @Override
                public String getName() {
                    return null;
                }
                @Override
                public Course getCourse() {
                    return null;
                }
                @Override
                public Iterable<Competitor> getCompetitors() {
                    return Collections.singleton(competitor);
                }
                @Override
                public BoatClass getBoatClass() {
                    return null;
                }
            };

        }

        @Override
        public boolean hasStarted(TimePoint at) {
            return started;
        }

        @Override
        public int getRank(Competitor competitor, TimePoint timePoint) throws NoWindException {
            return rank;
        }

        @Override
        public int getRank(Competitor competitor) throws NoWindException {
            return rank;
        }

        @Override
        public RaceDefinition getRace() {
            return raceDefinition;
        }
    }
}
