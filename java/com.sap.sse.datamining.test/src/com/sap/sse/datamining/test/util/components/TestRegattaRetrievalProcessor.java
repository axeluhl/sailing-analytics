package com.sap.sse.datamining.test.util.components;

import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractRetrievalProcessor;
import com.sap.sse.datamining.test.domain.Test_Regatta;

public class TestRegattaRetrievalProcessor extends AbstractRetrievalProcessor<Collection<Test_Regatta>, Test_Regatta> {

    @SuppressWarnings("unchecked")
    public TestRegattaRetrievalProcessor(ExecutorService executor,
            Collection<Processor<Test_Regatta, ?>> resultReceivers, int retrievalLevel,
            String retrievedDataTypeMessageKey) {
        super((Class<Collection<Test_Regatta>>) (Class<?>) Collection.class, Test_Regatta.class, executor,
                resultReceivers, retrievalLevel, retrievedDataTypeMessageKey);
    }

    @Override
    protected Iterable<Test_Regatta> retrieveData(Collection<Test_Regatta> regattas) {
        return regattas;
    }

}
