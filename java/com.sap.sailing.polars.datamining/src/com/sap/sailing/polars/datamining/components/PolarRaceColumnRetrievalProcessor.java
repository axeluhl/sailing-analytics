package com.sap.sailing.polars.datamining.components;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.polars.datamining.data.HasLeaderboardPolarContext;
import com.sap.sailing.polars.datamining.data.HasRaceColumnPolarContext;
import com.sap.sailing.polars.datamining.data.impl.RaceColumnWithPolarContext;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractRetrievalProcessor;

public class PolarRaceColumnRetrievalProcessor extends AbstractRetrievalProcessor<HasLeaderboardPolarContext, HasRaceColumnPolarContext> {

    public PolarRaceColumnRetrievalProcessor(ExecutorService executor,
            Collection<Processor<HasRaceColumnPolarContext, ?>> resultReceivers, int retrievalLevel) {
        super(HasLeaderboardPolarContext.class, HasRaceColumnPolarContext.class, executor, resultReceivers, retrievalLevel);
    }

    @Override
    protected Iterable<HasRaceColumnPolarContext> retrieveData(HasLeaderboardPolarContext element) {
        Set<HasRaceColumnPolarContext> raceColumnWithContext = new HashSet<>();
        Leaderboard leaderboard = element.getLeaderboard();
        for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
            if (isAborted()) {
                break;
            }
            raceColumnWithContext.add(new RaceColumnWithPolarContext(raceColumn, element));
        }
        return raceColumnWithContext;
    }

}
