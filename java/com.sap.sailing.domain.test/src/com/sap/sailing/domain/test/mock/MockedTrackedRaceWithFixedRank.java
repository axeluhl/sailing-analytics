package com.sap.sailing.domain.test.mock;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorWithBoat;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.CourseImpl;
import com.sap.sailing.domain.base.impl.DynamicCompetitorWithBoat;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.leaderboard.Leaderboard.RankComparableRank;
import com.sap.sailing.domain.leaderboard.impl.RankAndRankComparable;
import com.sap.sailing.domain.test.AbstractLeaderboardTest;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingAndORCPerformanceCurveCache;
import com.sap.sse.common.TimePoint;

public class MockedTrackedRaceWithFixedRank extends MockedTrackedRace {
    private static final long serialVersionUID = -8587203762630194172L;
    protected final Map<Competitor, Integer> ranks;
    private final boolean started;
    protected final LinkedHashMap<Competitor,Boat> competitorsAndBoats;
    private final BoatClass boatClass;
    protected RaceDefinition raceDefinition;

    /**
     * Initializes the race with {@code rank} competitors. {@link #getCompetitorsFromBestToWorst(TimePoint)} and
     * {@link #getCompetitorsFromBestToWorst(TimePoint, WindLegTypeAndLegBearingAndORCPerformanceCurveCache)} will return these
     * competitors such that {@code competitorWithBoat} is in zero-based position {@code rank-1}. Note that this requires adding
     * {@code rank-1} competitors before adding {@code competitorWithBoat}. These competitors are created here as proxy entries.
     */
    public MockedTrackedRaceWithFixedRank(CompetitorWithBoat competitorWithBoat, int rank, boolean started, BoatClass boatClass) {
        this.ranks = new HashMap<>();
        this.started = started;
        this.competitorsAndBoats = new LinkedHashMap<>();
        this.raceDefinition = new MockedRaceDefinition();
        this.boatClass = boatClass;
        for (int i=1; i<rank; i++) {
            final DynamicCompetitorWithBoat proxyCompetitor = AbstractLeaderboardTest.createCompetitorWithBoat("C"+i);
            this.competitorsAndBoats.put(proxyCompetitor, proxyCompetitor.getBoat());
            this.ranks.put(proxyCompetitor, i);
        }
        this.competitorsAndBoats.put(competitorWithBoat, competitorWithBoat.getBoat());
        this.ranks.put(competitorWithBoat, rank);
    }

    public MockedTrackedRaceWithFixedRank(CompetitorWithBoat competitorWithBoat, int rank, boolean started) {
        this(competitorWithBoat, rank, started, competitorWithBoat.getBoat().getBoatClass());
    }

    private class MockedRaceDefinition implements RaceDefinition {
        private static final long serialVersionUID = 6812543850545870357L;
        private final Course course;

        public MockedRaceDefinition() {
            course = new CourseImpl("Test Course", Arrays.asList(new Waypoint[0]));
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public Course getCourse() {
            return course;
        }

        @Override
        public Iterable<Competitor> getCompetitors() {
            return competitorsAndBoats.keySet();
        }

        @Override
        public Iterable<Boat> getBoats() {
            return competitorsAndBoats.values();
        }

        @Override
        public Map<Competitor, Boat> getCompetitorsAndTheirBoats() {
            return competitorsAndBoats;
        }

        @Override
        public BoatClass getBoatClass() {
            return boatClass;
        }

        @Override
        public Serializable getId() {
            return null;
        }

        @Override
        public Competitor getCompetitorById(Serializable competitorID) {
            Competitor c = competitorsAndBoats.keySet().iterator().next();
            if (competitorID.equals(c.getId())) {
                return competitorsAndBoats.keySet().iterator().next();
            } else {
                return null;
            }
        }

        @Override
        public Boat getBoatOfCompetitor(Competitor competitor) {
            return competitorsAndBoats.get(competitor);
        }

        @Override
        public byte[] getCompetitorMD5() {
            return null;
        }
    }

    @Override
    public Iterable<Competitor> getCompetitorsFromBestToWorst(TimePoint timePoint,
            WindLegTypeAndLegBearingAndORCPerformanceCurveCache cache) {
        return competitorsAndBoats.keySet();
    }

    @Override
    public LinkedHashMap<Competitor, RankAndRankComparable> getCompetitorsFromBestToWorstAndRankAndRankComparable(
            TimePoint timePoint, WindLegTypeAndLegBearingAndORCPerformanceCurveCache cache) {
        LinkedHashMap<Competitor, RankAndRankComparable> competitorsFromBestToWorst = new LinkedHashMap<Competitor, RankAndRankComparable>();
        for (final Competitor competitor : getCompetitorsFromBestToWorst(timePoint, cache)) {
            competitorsFromBestToWorst.put(competitor, new RankAndRankComparable(1, new RankComparableRank(1)));
        }
        return competitorsFromBestToWorst;
    }

    @Override
    public boolean hasStarted(TimePoint at) {
        return started;
    }

    @Override
    public int getRank(Competitor competitor, TimePoint timePoint, WindLegTypeAndLegBearingAndORCPerformanceCurveCache cache) {
        return ranks.get(competitor);
    }

    @Override
    public int getRank(Competitor competitor) throws NoWindException {
        return ranks.get(competitor);
    }

    @Override
    public RaceDefinition getRace() {
        return raceDefinition;
    }

    @Override
    public Boat getBoatOfCompetitor(Competitor competitor) {
        return competitorsAndBoats.get(competitor);
    }
}
