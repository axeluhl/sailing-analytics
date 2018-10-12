package com.sap.sse.datamining.test.util.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractRetrievalProcessor;
import com.sap.sse.datamining.test.data.Test_HasLegOfCompetitorContext;

public class Test_RetrievalProcessorWithSettings extends AbstractRetrievalProcessor<Test_HasLegOfCompetitorContext, Test_HasLegOfCompetitorContext>{

    private Test_RetrievalProcessorSettings settings;

    public Test_RetrievalProcessorWithSettings(ExecutorService executor, Collection<Processor<Test_HasLegOfCompetitorContext, ?>> resultReceivers,
            Test_RetrievalProcessorSettings settings, int retrievalLevel, String retrievedDataTypeMessageKey) {
        super(Test_HasLegOfCompetitorContext.class, Test_HasLegOfCompetitorContext.class, executor, resultReceivers, retrievalLevel, retrievedDataTypeMessageKey);
        this.settings = settings;
    }

    @Override
    protected Iterable<Test_HasLegOfCompetitorContext> retrieveData(Test_HasLegOfCompetitorContext legWithContext) {
        Collection<Test_HasLegOfCompetitorContext> legsWithContext = new ArrayList<>();
        legsWithContext.add(legWithContext);
        return legsWithContext;
    }
    
    public Test_RetrievalProcessorSettings getSettings() {
        return settings;
    }

}