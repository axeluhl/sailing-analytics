package com.sap.sailing.polars.datamining.components;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.polars.datamining.data.HasCompetitorPolarContext;
import com.sap.sailing.polars.datamining.data.HasLegPolarContext;
import com.sap.sailing.polars.datamining.data.impl.CompetitorWithPolarContext;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractRetrievalProcessor;

public class PolarCompetitorRetrievalProcessor extends AbstractRetrievalProcessor<HasLegPolarContext, HasCompetitorPolarContext> {

    public PolarCompetitorRetrievalProcessor(ExecutorService executor,
            Collection<Processor<HasCompetitorPolarContext, ?>> resultReceivers, int retrievalLevel) {
        super(HasLegPolarContext.class, HasCompetitorPolarContext.class, executor, resultReceivers, retrievalLevel);
    }

    @Override
    protected Iterable<HasCompetitorPolarContext> retrieveData(HasLegPolarContext element) {
        TrackedRace trackedRace = element.getTrackedRace();
        Set<HasCompetitorPolarContext> competitorWithContext = new HashSet<>();
        for (Competitor competitor : trackedRace.getRace().getCompetitors()) {
            if (isAborted()) {
                break;
            }
            competitorWithContext.add(new CompetitorWithPolarContext(competitor, trackedRace, element.getLeg(), element));
        }
        return competitorWithContext;
    }

}
