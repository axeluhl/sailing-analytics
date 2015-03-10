package com.sap.sailing.datamining.impl.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.datamining.data.HasRaceResultOfCompetitorContext;
import com.sap.sailing.datamining.data.HasLeaderboardContext;
import com.sap.sailing.datamining.impl.data.RaceResultOfCompetitorWithContext;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractSimpleRetrievalProcessor;

public class CompetitorOfTrackedRaceRetrievalProcessor extends
        AbstractSimpleRetrievalProcessor<HasLeaderboardContext, HasRaceResultOfCompetitorContext> {

    public CompetitorOfTrackedRaceRetrievalProcessor(ExecutorService executor,
            Collection<Processor<HasRaceResultOfCompetitorContext, ?>> resultReceivers) {
        super(HasLeaderboardContext.class, HasRaceResultOfCompetitorContext.class, executor, resultReceivers);
    }

    @Override
    protected Iterable<HasRaceResultOfCompetitorContext> retrieveData(HasLeaderboardContext element) {
        Collection<HasRaceResultOfCompetitorContext> raceResultsOfCompetitor = new ArrayList<>();
        for (RaceColumn raceColumn : element.getLeaderboard().getRaceColumns()) {
            for (Competitor competitor : element.getLeaderboard().getCompetitors()) {
                HasRaceResultOfCompetitorContext raceResultOfCompetitorContext = new RaceResultOfCompetitorWithContext(element, raceColumn, competitor);
                raceResultsOfCompetitor.add(raceResultOfCompetitorContext);
            }
        }
        return raceResultsOfCompetitor;
    }

}
