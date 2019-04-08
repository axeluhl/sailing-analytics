package com.sap.sailing.datamining.impl.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.datamining.data.HasRaceOfCompetitorContext;
import com.sap.sailing.datamining.data.HasTrackedRaceContext;
import com.sap.sailing.datamining.impl.data.RaceOfCompetitorWithContext;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractRetrievalProcessor;

public class RaceOfCompetitorRetrievalProcessor extends AbstractRetrievalProcessor<HasTrackedRaceContext, HasRaceOfCompetitorContext> {

    public RaceOfCompetitorRetrievalProcessor(ExecutorService executor,
            Collection<Processor<HasRaceOfCompetitorContext, ?>> resultReceivers, int retrievalLevel,
            String retrievedDataTypeMessageKey) {
        super(HasTrackedRaceContext.class, HasRaceOfCompetitorContext.class, executor, resultReceivers, retrievalLevel,
                retrievedDataTypeMessageKey);
    }

    @Override
    protected Iterable<HasRaceOfCompetitorContext> retrieveData(HasTrackedRaceContext element) {
        Collection<HasRaceOfCompetitorContext> raceOfCompetitorsWithContext = new ArrayList<>();
        for (Competitor competitor : element.getTrackedRace().getRace().getCompetitors()) {
            if (isAborted()) {
                break;
            }
            HasRaceOfCompetitorContext raceOfCompetitorWithContext = new RaceOfCompetitorWithContext(element, competitor);
            raceOfCompetitorsWithContext.add(raceOfCompetitorWithContext);
        }
        return raceOfCompetitorsWithContext;
    }

}
