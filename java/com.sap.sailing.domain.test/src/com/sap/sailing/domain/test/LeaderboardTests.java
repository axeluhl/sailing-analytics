package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashSet;
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
import com.sap.sailing.domain.leaderboard.ScoreCorrection;
import com.sap.sailing.domain.leaderboard.impl.LeaderboardImpl;
import com.sap.sailing.domain.leaderboard.impl.ResultDiscardingRuleImpl;
import com.sap.sailing.domain.test.mock.MockedTrackedRace;
import com.sap.sailing.domain.tracking.NoWindException;
import com.sap.sailing.domain.tracking.TrackedRace;

public class LeaderboardTests {
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
            TrackedRace r = new MockedTrackedRaceWithFixedRank(i, /* started */ true);
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
        setupRaces(2, 9);
        Leaderboard leaderboard = new LeaderboardImpl(new ScoreCorrection() {
            @Override
            public int getCorrectedScore(int uncorrectedScore, Competitor competitor, TrackedRace trackedRace,
                    TimePoint timePoint) {
                return uncorrectedScore;
            }
        }, new ResultDiscardingRuleImpl(new int[] { 3, 6 }));
        for (TrackedRace race : testRaces) {
            leaderboard.addRace(race);
        }
        TimePoint now = MillisecondsTimePoint.now();
        for (TrackedRace race : testRaces) {
            if (race.hasStarted(now)) {
                assertEquals(race.getRank(competitor, now), leaderboard.getTrackedPoints(competitor, race, now));
                assertEquals(race.getRank(competitor, now), leaderboard.getNetPoints(competitor, race, now));
            } else {
                assertEquals(0, leaderboard.getTrackedPoints(competitor, race, now));
                assertEquals(0, leaderboard.getNetPoints(competitor, race, now));
            }
        }
    }
    
    private class MockedTrackedRaceWithFixedRank extends MockedTrackedRace {
        private final int rank;
        private final boolean started;
        
        public MockedTrackedRaceWithFixedRank(int rank, boolean started) {
            this.rank = rank;
            this.started = started;
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
    }
}
