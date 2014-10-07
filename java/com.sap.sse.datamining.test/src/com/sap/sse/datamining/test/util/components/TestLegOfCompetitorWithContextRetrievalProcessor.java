package com.sap.sse.datamining.test.util.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractSimpleRetrievalProcessor;
import com.sap.sse.datamining.test.functions.registry.test_classes.Test_Competitor;
import com.sap.sse.datamining.test.functions.registry.test_classes.Test_Leg;
import com.sap.sse.datamining.test.functions.registry.test_contexts.Test_HasLegOfCompetitorContext;
import com.sap.sse.datamining.test.functions.registry.test_contexts.Test_HasLegOfCompetitorContextImpl;
import com.sap.sse.datamining.test.functions.registry.test_contexts.Test_HasRaceContext;

public class TestLegOfCompetitorWithContextRetrievalProcessor extends
        AbstractSimpleRetrievalProcessor<Test_HasRaceContext, Test_HasLegOfCompetitorContext>{

    public TestLegOfCompetitorWithContextRetrievalProcessor(ExecutorService executor, Collection<Processor<Test_HasLegOfCompetitorContext, ?>> resultReceivers) {
        super(Test_HasRaceContext.class, Test_HasLegOfCompetitorContext.class, executor, resultReceivers);
    }

    @Override
    protected Iterable<Test_HasLegOfCompetitorContext> retrieveData(Test_HasRaceContext raceWithContext) {
        Collection<Test_HasLegOfCompetitorContext> legsWithContext = new ArrayList<>();
        int legNumber = 0;
        for (Test_Leg leg : raceWithContext.getRace().getLegs()) {
            legNumber++;
            for (Test_Competitor competitor : raceWithContext.getRace().getCompetitors()) {
                legsWithContext.add(new Test_HasLegOfCompetitorContextImpl(raceWithContext.getRegatta(), raceWithContext.getRace(), raceWithContext.getBoatClass(),
                                                               raceWithContext.getYear(), leg, legNumber, competitor));
            }
        }
        return legsWithContext;
    }

}
