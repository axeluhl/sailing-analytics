package com.sap.sse.datamining.test.util.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.FilterCriterion;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractSimpleFilteringRetrievalProcessor;
import com.sap.sse.datamining.test.functions.registry.test_classes.Test_Race;
import com.sap.sse.datamining.test.functions.registry.test_classes.Test_Regatta;
import com.sap.sse.datamining.test.functions.registry.test_contexts.Test_HasRaceContext;
import com.sap.sse.datamining.test.functions.registry.test_contexts.Test_HasRaceContextImpl;

public class TestRaceWithContextFilteringRetrievalProcessor extends
        AbstractSimpleFilteringRetrievalProcessor<Test_Regatta, Test_HasRaceContext> {

    public TestRaceWithContextFilteringRetrievalProcessor(ExecutorService executor,
            Collection<Processor<Test_HasRaceContext>> resultReceivers, FilterCriterion<Test_HasRaceContext> criteria) {
        super(Test_Regatta.class, executor, resultReceivers, criteria);
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
