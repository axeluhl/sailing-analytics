package com.sap.sailing.datamining.impl.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.datamining.data.HasLeaderboardContext;
import com.sap.sailing.datamining.data.HasRaceResultOfCompetitorContext;
import com.sap.sailing.datamining.impl.data.RaceResultOfCompetitorWithContext;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractRetrievalProcessor;

public class CompetitorOfRaceInLeaderboardRetrievalProcessor extends
        AbstractRetrievalProcessor<HasLeaderboardContext, HasRaceResultOfCompetitorContext> {

    public CompetitorOfRaceInLeaderboardRetrievalProcessor(ExecutorService executor,
            Collection<Processor<HasRaceResultOfCompetitorContext, ?>> resultReceivers, int retrievalLevel) {
        super(HasLeaderboardContext.class, HasRaceResultOfCompetitorContext.class, executor, resultReceivers, retrievalLevel);
    }

    @Override
    protected Iterable<HasRaceResultOfCompetitorContext> retrieveData(HasLeaderboardContext element) {
        Collection<HasRaceResultOfCompetitorContext> raceResultsOfCompetitor = new ArrayList<>();
        for (RaceColumn raceColumn : element.getLeaderboard().getRaceColumns()) {
            if (isAborted()) {
                break;
            }
            for (Competitor competitor : element.getLeaderboard().getCompetitors()) {
                if (isAborted()) {
                    break;
                }
                HasRaceResultOfCompetitorContext raceResultOfCompetitorContext = new RaceResultOfCompetitorWithContext(element, raceColumn, competitor,
                		element.getLeaderboardGroupContext().getPolarDataService());
                raceResultsOfCompetitor.add(raceResultOfCompetitorContext);
            }
        }
        return raceResultsOfCompetitor;
    }

}
