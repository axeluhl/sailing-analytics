package com.sap.sailing.domain.test;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.RaceDefinition;

public class MockedTrackedRaceWithFixedRankAndManyCompetitors extends MockedTrackedRaceWithFixedRank {
    private final Set<Competitor> competitors;
    private final RaceDefinition raceDefinition;
    
    public MockedTrackedRaceWithFixedRankAndManyCompetitors(Competitor competitor, int rank, boolean started) {
        super(competitor, rank, started);
        competitors = new HashSet<Competitor>();
        competitors.add(competitor);
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
                return competitors;
            }
            @Override
            public BoatClass getBoatClass() {
                return null;
            }
        };
    }
    
    public void addCompetitor(Competitor competitor) {
        competitors.add(competitor);
    }

    @Override
    public RaceDefinition getRace() {
        return raceDefinition;
    }

    
}
