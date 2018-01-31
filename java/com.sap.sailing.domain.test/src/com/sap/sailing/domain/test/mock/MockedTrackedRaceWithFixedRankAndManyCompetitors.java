package com.sap.sailing.domain.test.mock;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.RaceDefinition;

public class MockedTrackedRaceWithFixedRankAndManyCompetitors extends MockedTrackedRaceWithFixedRank {
    private static final long serialVersionUID = -6189437610363552577L;
    private final Set<Competitor> competitors;
    private final RaceDefinition raceDefinition;
    
    public MockedTrackedRaceWithFixedRankAndManyCompetitors(Competitor competitor, int rank, boolean started) {
        super(competitor, rank, started);
        competitors = new HashSet<Competitor>();
        competitors.add(competitor);
        this.raceDefinition = new RaceDefinition() {
            private static final long serialVersionUID = 8878878939443845646L;
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
            public Competitor getCompetitorById(Serializable competitorID) {
                for (Competitor competitor : competitors) {
                    if (competitorID.equals(competitor.getId())) {
                        return competitor;
                    }
                }
                return null;
            }

            @Override
            public BoatClass getBoatClass() {
                return null;
            }
            @Override
            public Serializable getId() {
                return null;
            }
            @Override
            public Boat getBoatOfCompetitor(Competitor competitor) {
                return null;
            }
            @Override
            public byte[] getCompetitorMD5() {
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
