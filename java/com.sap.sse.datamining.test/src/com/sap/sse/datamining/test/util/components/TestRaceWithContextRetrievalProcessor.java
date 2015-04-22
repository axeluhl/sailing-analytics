package com.sap.sse.datamining.test.util.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractRetrievalProcessor;
import com.sap.sse.datamining.test.functions.registry.test_classes.Test_Race;
import com.sap.sse.datamining.test.functions.registry.test_classes.Test_Regatta;
import com.sap.sse.datamining.test.functions.registry.test_contexts.Test_HasRaceContext;
import com.sap.sse.datamining.test.functions.registry.test_contexts.Test_HasRaceContextImpl;

public class TestRaceWithContextRetrievalProcessor extends AbstractRetrievalProcessor<Test_Regatta, Test_HasRaceContext> {

    public TestRaceWithContextRetrievalProcessor(ExecutorService executor,
            Collection<Processor<Test_HasRaceContext, ?>> resultReceivers, int retrievalLevel) {
        super(Test_Regatta.class, Test_HasRaceContext.class, executor, resultReceivers, retrievalLevel);
    }

    @Override
    protected Iterable<Test_HasRaceContext> retrieveData(Test_Regatta regatta) {
        Collection<Test_HasRaceContext> racesWithContext = new ArrayList<>();
        for (Test_Race race : regatta.getRaces()) {
            racesWithContext.add(new Test_HasRaceContextImpl(regatta, race, regatta.getBoatClass(), regatta.getYear()));
        }
        return racesWithContext;
    }

}
