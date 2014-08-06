package com.sap.sse.datamining.impl.components;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.datamining.AdditionalResultDataBuilder;
import com.sap.sse.datamining.components.FilterCriteria;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.test.util.ConcurrencyTestsUtil;

public class TestAbstractFilteringRetrievalProcessor {
    
    private Processor<Iterable<Integer>> filteringRetrievalProcessor;
    private Collection<Integer> dataSource;
    
    private Collection<Integer> receivedResults = new HashSet<>();
    
    @Before
    public void setUpDataSource() {
        dataSource = Arrays.asList(-2, -1, 0, 1, 2);
    }

    @Test
    public void testFiltration() throws InterruptedException {
        filteringRetrievalProcessor.processElement(dataSource);
        filteringRetrievalProcessor.finish();
        ConcurrencyTestsUtil.sleepFor(500); //Giving the processor time to finish

        Collection<Integer> expectedResults = new HashSet<>(Arrays.asList(0, 1, 2));
        assertThat(receivedResults, is(expectedResults));
    }
    
    @Test
    public void testProvidedAdditionalData() throws InterruptedException {
        filteringRetrievalProcessor.processElement(dataSource);
        filteringRetrievalProcessor.finish();
        ConcurrencyTestsUtil.sleepFor(500); //Giving the processor time to finish
        
        int expectedRetrievedDataAmount = 5;
        
        SumBuildingAndOverwritingResultDataBuilder resultDataBuilder = new SumBuildingAndOverwritingResultDataBuilder();
        filteringRetrievalProcessor.getAdditionalResultData(resultDataBuilder);
        assertThat(resultDataBuilder.getRetrievedDataAmount(), is(expectedRetrievedDataAmount));
    }
    
    @Test
    public void testProvidedAdditionalDataWithMultipleRetrievalLayers() throws InterruptedException {
//        fail("Not yet implemented");
//        filteringRetrievalProcessor.processElement(dataSource);
//        filteringRetrievalProcessor.finish();
//        ConcurrencyTestsUtil.sleepFor(500); //Giving the processor time to finish
//        
//        int expectedRetrievedDataAmount = 5;
//        
//        SumBuildingAndOverwritingResultDataBuilder resultDataBuilder = new SumBuildingAndOverwritingResultDataBuilder();
//        filteringRetrievalProcessor.getAdditionalResultData(resultDataBuilder);
//        assertThat(resultDataBuilder.getRetrievedDataAmount(), is(expectedRetrievedDataAmount));
    }
    
    @Before
    public void setUpResultReceiverAndProcessor() {
        Processor<Integer> resultReceiver = new Processor<Integer>() {
            @Override
            public void processElement(Integer element) {
                synchronized (receivedResults) {
                    receivedResults.add(element);
                }
            }
            @Override
            public void onFailure(Throwable failure) {
            }
            @Override
            public void finish() throws InterruptedException {
            }
            @Override
            public void abort() {
            }
            @Override
            public AdditionalResultDataBuilder getAdditionalResultData(AdditionalResultDataBuilder additionalDataBuilder) {
                return additionalDataBuilder;
            }
        };
        
        FilterCriteria<Integer> elementGreaterZeroFilterCriteria = new FilterCriteria<Integer>() {
            @Override
            public boolean matches(Integer element) {
                return element >= 0;
            }
        };
        filteringRetrievalProcessor = new AbstractSimpleFilteringRetrievalProcessor<Iterable<Integer>, Integer>(ConcurrencyTestsUtil.getExecutor(), Arrays.asList(resultReceiver), elementGreaterZeroFilterCriteria) {
            @Override
            protected Iterable<Integer> retrieveData(Iterable<Integer> element) {
                return element;
            }
        };
    }

}
