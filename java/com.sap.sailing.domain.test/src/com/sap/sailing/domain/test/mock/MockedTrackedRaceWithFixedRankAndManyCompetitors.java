package com.sap.sailing.domain.test.mock;

import java.io.Serializable;
import java.util.Map;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorWithBoat;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.RaceDefinition;

public class MockedTrackedRaceWithFixedRankAndManyCompetitors extends MockedTrackedRaceWithFixedRank {
    private static final long serialVersionUID = -6189437610363552577L;
    
    public MockedTrackedRaceWithFixedRankAndManyCompetitors(CompetitorWithBoat competitorWithBoat, int rank, boolean started) {
        super(competitorWithBoat, rank, started);
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
            public Competitor getCompetitorById(Serializable competitorID) {
                for (Competitor competitor : competitorsAndBoats.keySet()) {
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
                return competitorsAndBoats.get(competitor);
            }
            @Override
            public byte[] getCompetitorMD5() {
                return null;
            }
        };
    }
    
    public void addCompetitorWithBoat(CompetitorWithBoat competitorWithBoat) {
        competitorsAndBoats.put(competitorWithBoat, competitorWithBoat.getBoat());
    }

    @Override
    public RaceDefinition getRace() {
        return raceDefinition;
    }

    
}
