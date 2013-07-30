package com.sap.sailing.datamining.impl.retrievers;

import java.util.Collection;

import com.google.gwt.dev.util.collect.HashSet;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.tracking.TrackedRace;

public class CompetitorNameDataRetriever extends CompetitorDetailDataRetriever {

    private Collection<String> competitorNames;

    public CompetitorNameDataRetriever(TrackedRace trackedRace, Collection<String> competitorNames) {
        super(trackedRace);
        this.competitorNames = new HashSet<String>(competitorNames);
    }

    @Override
    protected boolean retrieveDataFor(Competitor competitor) {
        for (String competitorName : competitorNames) {
            if (competitorName.equals(competitor.getName())) {
                return true;
            }
        }
        return false;
    }

}
