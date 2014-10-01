package com.sap.sse.datamining.impl.components;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.datamining.DataRetrieverChainBuilder;
import com.sap.sse.datamining.DataRetrieverChainDefinition;
import com.sap.sse.datamining.components.FilterCriterion;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.AbstractDataRetrieverChainDefinition;
import com.sap.sse.datamining.impl.criterias.AbstractFilterCriterion;
import com.sap.sse.datamining.test.functions.registry.test_classes.Test_Regatta;
import com.sap.sse.datamining.test.functions.registry.test_contexts.Test_HasContextWithDeadConnectorEnd;
import com.sap.sse.datamining.test.functions.registry.test_contexts.Test_HasLegOfCompetitorContext;
import com.sap.sse.datamining.test.functions.registry.test_contexts.Test_HasRaceContext;
import com.sap.sse.datamining.test.util.ComponentsAndQueriesTestsUtil;
import com.sap.sse.datamining.test.util.ConcurrencyTestsUtil;
import com.sap.sse.datamining.test.util.components.NullProcessor;
import com.sap.sse.datamining.test.util.components.TestLegOfCompetitorWithContextFilteringRetrievalProcessor;
import com.sap.sse.datamining.test.util.components.TestRaceWithContextFilteringRetrievalProcessor;
import com.sap.sse.datamining.test.util.components.TestRegattaRetrievalProcessor;

public class TestDataRetrieverChainCreation {

    private List<Class<?>> retrievableDataTypes;
    private DataRetrieverChainDefinition<Collection<Test_Regatta>> dataRetrieverChainDefinition;
    
    private FilterCriterion<Test_HasRaceContext> raceFilter;
    private FilterCriterion<Test_HasLegOfCompetitorContext> legOfCompetitorFilter;
    
    private int receivedLegOfCompetitorAmount = 0;
    private Processor<Test_HasLegOfCompetitorContext, Void> legReceiver = new NullProcessor<Test_HasLegOfCompetitorContext, Void>(Test_HasLegOfCompetitorContext.class, Void.class) {
        @Override
        public void processElement(Test_HasLegOfCompetitorContext element) {
            receivedLegOfCompetitorAmount++;
        }
    };
    
    @Before
    public void initializeRetrieverChain() {
        retrievableDataTypes = new ArrayList<>();
        retrievableDataTypes.add(Test_Regatta.class);
        retrievableDataTypes.add(Test_HasRaceContext.class);
        retrievableDataTypes.add(Test_HasLegOfCompetitorContext.class);
        
        dataRetrieverChainDefinition = new AbstractDataRetrieverChainDefinition<Collection<Test_Regatta>>(Collection.class, retrievableDataTypes) {
            @Override
            protected Processor<?, ?> createRetrieverFor(Class<?> retrievedDataType, FilterCriterion<?> filter,
                    Collection<Processor<?, ?>> resultReceivers) {
                ThreadPoolExecutor executor = ConcurrencyTestsUtil.getExecutor();
                
                if (retrievedDataType.equals(Test_Regatta.class)) {
                    Collection<Processor<Test_Regatta, ?>> specificResultReceivers = (Collection<Processor<Test_Regatta, ?>>)(Collection<?>) resultReceivers;
                    return new TestRegattaRetrievalProcessor(executor, specificResultReceivers);
                }
                if (retrievedDataType.equals(Test_HasRaceContext.class)) {
                    Collection<Processor<Test_HasRaceContext, ?>> specificResultReceivers = (Collection<Processor<Test_HasRaceContext, ?>>)(Collection<?>) resultReceivers;
                    FilterCriterion<Test_HasRaceContext> specificFilter = (FilterCriterion<Test_HasRaceContext>) filter;
                    return new TestRaceWithContextFilteringRetrievalProcessor(executor, specificResultReceivers, specificFilter);
                }
                if (retrievedDataType.equals(Test_HasLegOfCompetitorContext.class)) {
                    Collection<Processor<Test_HasLegOfCompetitorContext, ?>> specificResultReceivers = (Collection<Processor<Test_HasLegOfCompetitorContext, ?>>)(Collection<?>) resultReceivers;
                    FilterCriterion<Test_HasLegOfCompetitorContext> specificFilter = (FilterCriterion<Test_HasLegOfCompetitorContext>) filter;
                    return new TestLegOfCompetitorWithContextFilteringRetrievalProcessor(executor, specificResultReceivers, specificFilter);
                }
                throw new IllegalArgumentException("Creation of a retriever for the type '" + retrievedDataType.getSimpleName()
                                                   + "' isn't implemented.");
            }
            
        };
    }
    
    @Before
    public void initializeFilters() {
        raceFilter = new AbstractFilterCriterion<Test_HasRaceContext>(Test_HasRaceContext.class) {
            @Override
            public boolean matches(Test_HasRaceContext element) {
                return element.getBoatClass().getName().equals("49er");
            }
        };
        
        legOfCompetitorFilter = new AbstractFilterCriterion<Test_HasLegOfCompetitorContext>(Test_HasLegOfCompetitorContext.class) {
            @Override
            public boolean matches(Test_HasLegOfCompetitorContext element) {
                return element.getLegNumber() <= 3;
            }
        };
    }

    @Test
    public void testStepByStepDataRetrieverChainCreation() throws InterruptedException {
        DataRetrieverChainBuilder<Collection<Test_Regatta>> chainBuilder = dataRetrieverChainDefinition.startBuilding();
        assertThat(chainBuilder.getCurrentRetrievedDataType().equals(Test_Regatta.class), is(true));
        
        chainBuilder.stepDeeper().setFilter(raceFilter);
        assertThat(chainBuilder.getCurrentRetrievedDataType().equals(Test_HasRaceContext.class), is(true));
        
        chainBuilder.stepDeeper().setFilter(legOfCompetitorFilter).addResultReceiver(legReceiver);
        assertThat(chainBuilder.getCurrentRetrievedDataType().equals(Test_HasLegOfCompetitorContext.class), is(true));
        
        Processor<Collection<Test_Regatta>, ?> firstRetrieverInChain = chainBuilder.build();
        firstRetrieverInChain.processElement(ComponentsAndQueriesTestsUtil.createExampleDataSource());
        firstRetrieverInChain.finish();
        ConcurrencyTestsUtil.sleepFor(500);
        assertThat(receivedLegOfCompetitorAmount, is(3 /*races*/ * 3 /*legs*/ * 4 /*competitors*/));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testSettingAFilterWithWrongElementType() {
        dataRetrieverChainDefinition.startBuilding().setFilter(raceFilter);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testSettingAResultReceiverWithWrongInputType() {
        dataRetrieverChainDefinition.startBuilding().addResultReceiver(legReceiver);
    }

    @Test
    public void testSupportedDataTypes() {
        for (Class<?> dataType : retrievableDataTypes) {
            assertThat(dataRetrieverChainDefinition.canRetrieve(dataType), is(true));
        }

        assertThat(dataRetrieverChainDefinition.canRetrieve(Test_HasContextWithDeadConnectorEnd.class), is(false));
    }

}
