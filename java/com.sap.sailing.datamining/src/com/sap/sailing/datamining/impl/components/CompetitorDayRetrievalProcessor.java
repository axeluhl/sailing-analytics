package com.sap.sailing.datamining.impl.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.datamining.data.HasCompetitorDayContext;
import com.sap.sailing.datamining.data.HasRaceOfCompetitorContext;
import com.sap.sailing.datamining.impl.data.CompetitorDayWithContext;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractRetrievalProcessor;

public class CompetitorDayRetrievalProcessor extends AbstractRetrievalProcessor<HasRaceOfCompetitorContext, HasCompetitorDayContext> {

    public CompetitorDayRetrievalProcessor(ExecutorService executor,
            Collection<Processor<HasCompetitorDayContext, ?>> resultReceivers, int retrievalLevel,
            String retrievedDataTypeMessageKey) {
        super(HasRaceOfCompetitorContext.class, HasCompetitorDayContext.class, executor, resultReceivers, retrievalLevel,
                retrievedDataTypeMessageKey);
    }

    @Override
    protected Iterable<HasCompetitorDayContext> retrieveData(HasRaceOfCompetitorContext element) {
        Collection<HasCompetitorDayContext> raceOfCompetitorsWithContext = new ArrayList<>();
        if (element.getCompetitor() != null) {
            HasCompetitorDayContext raceOfCompetitorWithContext = new CompetitorDayWithContext(element);
            raceOfCompetitorsWithContext.add(raceOfCompetitorWithContext);
        }
        return raceOfCompetitorsWithContext;
    }
}
