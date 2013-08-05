package com.sap.sailing.datamining.impl.retrievers;

import java.util.Collection;
import java.util.HashSet;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.tracking.TrackedRace;

public class CompetitorNationalityDataRetriever extends TrackedRaceDataRetriever {

    private Collection<String> nationalities;

    public CompetitorNationalityDataRetriever(TrackedRace trackedRace, Collection<String> nationalities) {
        super(trackedRace);
        this.nationalities = new HashSet<String>(nationalities);
    }

    @Override
    protected boolean retrieveDataFor(Competitor competitor) {
        for (String nationality : nationalities) {
            if (nationality.equals(competitor.getTeam().getNationality().getThreeLetterIOCAcronym())) {
                return true;
            }
        }
        return false;
    }

}
