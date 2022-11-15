package com.sap.sailing.datamining.impl.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.datamining.data.HasLeaderboardContext;
import com.sap.sailing.datamining.data.HasRaceResultOfCompetitorContext;
import com.sap.sailing.datamining.data.HasTrackedRaceContext;
import com.sap.sailing.datamining.impl.data.RaceResultOfCompetitorWithContext;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractRetrievalProcessor;

public class CompetitorOfRaceInLeaderboardRetrievalProcessor
        extends AbstractRetrievalProcessor<HasTrackedRaceContext, HasRaceResultOfCompetitorContext> {

    public CompetitorOfRaceInLeaderboardRetrievalProcessor(ExecutorService executor,
            Collection<Processor<HasRaceResultOfCompetitorContext, ?>> resultReceivers, int retrievalLevel,
            String retrievedDataTypeMessageKey) {
        super(HasTrackedRaceContext.class, HasRaceResultOfCompetitorContext.class, executor, resultReceivers,
                retrievalLevel, retrievedDataTypeMessageKey);
    }

    @Override
    protected Iterable<HasRaceResultOfCompetitorContext> retrieveData(HasTrackedRaceContext element) {
        Collection<HasRaceResultOfCompetitorContext> raceResultsOfCompetitor = new ArrayList<>();
        HasLeaderboardContext leaderboardContext = element.getLeaderboardContext();
        for (RaceColumn raceColumn : leaderboardContext.getLeaderboard().getRaceColumns()) {
            if (isAborted()) {
                break;
            }
            for (Competitor competitor : leaderboardContext.getLeaderboard().getCompetitors()) {
                if (isAborted()) {
                    break;
                }
                HasRaceResultOfCompetitorContext raceResultOfCompetitorContext = new RaceResultOfCompetitorWithContext(
                        leaderboardContext, raceColumn, competitor,
                        leaderboardContext.getLeaderboardGroupContext().getPolarDataService(), element);
                raceResultsOfCompetitor.add(raceResultOfCompetitorContext);
            }
        }
        return raceResultsOfCompetitor;
    }

}
