package com.sap.sailing.datamining.impl.components;

import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.datamining.data.HasCompetitorContext;
import com.sap.sailing.datamining.data.HasLeaderboardContext;
import com.sap.sailing.datamining.impl.data.CompetitorWithContext;
import com.sap.sse.common.Util;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractRetrievalProcessor;

public class CompetitorRetrievalProcessor extends AbstractRetrievalProcessor<HasLeaderboardContext, HasCompetitorContext> {
    public CompetitorRetrievalProcessor(ExecutorService executor,
            Collection<Processor<HasCompetitorContext, ?>> resultReceivers, int retrievalLevel,
            String retrievedDataTypeMessageKey) {
        super(HasLeaderboardContext.class, HasCompetitorContext.class, executor, resultReceivers, retrievalLevel,
                retrievedDataTypeMessageKey);
    }

    @Override
    protected Iterable<HasCompetitorContext> retrieveData(HasLeaderboardContext element) {
        return Util.map(element.getLeaderboard().getCompetitors(), c->new CompetitorWithContext(c, element));
    }
}
