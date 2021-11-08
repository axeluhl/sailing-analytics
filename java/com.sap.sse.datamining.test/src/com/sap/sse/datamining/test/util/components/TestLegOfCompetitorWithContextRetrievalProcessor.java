package com.sap.sse.datamining.test.util.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractRetrievalProcessor;
import com.sap.sse.datamining.test.data.Test_HasLegOfCompetitorContext;
import com.sap.sse.datamining.test.data.Test_HasLegOfCompetitorContextImpl;
import com.sap.sse.datamining.test.data.Test_HasRaceContext;
import com.sap.sse.datamining.test.domain.Test_Competitor;
import com.sap.sse.datamining.test.domain.Test_Leg;

public class TestLegOfCompetitorWithContextRetrievalProcessor extends AbstractRetrievalProcessor<Test_HasRaceContext, Test_HasLegOfCompetitorContext>{

    public TestLegOfCompetitorWithContextRetrievalProcessor(ExecutorService executor,
            Collection<Processor<Test_HasLegOfCompetitorContext, ?>> resultReceivers, int retrievalLevel,
            String retrievedDataTypeMessageKey) {
        super(Test_HasRaceContext.class, Test_HasLegOfCompetitorContext.class, executor, resultReceivers,
                retrievalLevel, retrievedDataTypeMessageKey);
    }

    @Override
    protected Iterable<Test_HasLegOfCompetitorContext> retrieveData(Test_HasRaceContext raceWithContext) {
        Collection<Test_HasLegOfCompetitorContext> legsWithContext = new ArrayList<>();
        int legNumber = 0;
        for (Test_Leg leg : raceWithContext.getRace().getLegs()) {
            legNumber++;
            for (Test_Competitor competitor : raceWithContext.getRace().getCompetitors()) {
                legsWithContext.add(new Test_HasLegOfCompetitorContextImpl(raceWithContext, leg, legNumber, competitor));
            }
        }
        return legsWithContext;
    }

}
