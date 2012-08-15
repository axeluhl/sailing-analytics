package com.sap.sailing.domain.test.mock;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.TimePoint;

public class MockedTrackedRaceWithFixedRank extends MockedTrackedRace {
    private static final long serialVersionUID = -8587203762630194172L;
    private final int rank;
    private final boolean started;
    private final RaceDefinition raceDefinition;
    private final Competitor competitor;
    private final BoatClass boatClass;
    
    public MockedTrackedRaceWithFixedRank(Competitor competitor, int rank, boolean started, BoatClass boatClass) {
        this.rank = rank;
        this.started = started;
        this.competitor = competitor;
        this.raceDefinition = new MockedRaceDefinition();
        this.boatClass = boatClass;
    }

    public MockedTrackedRaceWithFixedRank(Competitor competitor, int rank, boolean started) {
        this(competitor, rank, started, /* boatClass */ null);
    }

    private class MockedRaceDefinition implements RaceDefinition {
        private static final long serialVersionUID = 6812543850545870357L;
        
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
            return Collections.singleton(MockedTrackedRaceWithFixedRank.this.competitor);
        }

        @Override
        public BoatClass getBoatClass() {
            return boatClass;
        }

        @Override
        public Serializable getId() {
            return null;
        }
    }
    
    @Override
    public List<Competitor> getCompetitorsFromBestToWorst(TimePoint timePoint) {
        return Collections.singletonList(competitor);
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
