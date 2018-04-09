package com.sap.sailing.domain.test.mock;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorWithBoat;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.CourseImpl;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sse.common.TimePoint;

public class MockedTrackedRaceWithFixedRank extends MockedTrackedRace {
    private static final long serialVersionUID = -8587203762630194172L;
    private final int rank;
    private final boolean started;
    protected final Map<Competitor,Boat> competitorsAndBoats;
    private final BoatClass boatClass;
    protected RaceDefinition raceDefinition;
    
    public MockedTrackedRaceWithFixedRank(CompetitorWithBoat competitorWithBoat, int rank, boolean started, BoatClass boatClass) {
        this.rank = rank;
        this.started = started;
        this.competitorsAndBoats = new HashMap<>();
        this.raceDefinition = new MockedRaceDefinition();
        this.boatClass = boatClass;
        this.competitorsAndBoats.put(competitorWithBoat, competitorWithBoat.getBoat());
    }

    public MockedTrackedRaceWithFixedRank(CompetitorWithBoat competitorWithBoat,int rank, boolean started) {
        this(competitorWithBoat, rank, started, /* boatClass */ null);
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
    public List<Competitor> getCompetitorsFromBestToWorst(TimePoint timePoint) {
        return Collections.singletonList(competitorsAndBoats.keySet().iterator().next());
    }

    @Override
    public boolean hasStarted(TimePoint at) {
        return started;
    }

    @Override
    public int getRank(Competitor competitor, TimePoint timePoint) {
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
    
    @Override
    public Boat getBoatOfCompetitor(Competitor competitor) {
        return competitorsAndBoats.get(competitor);
    }
}
