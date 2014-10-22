package com.sap.sse.datamining.impl;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.datamining.DataRetrieverChainBuilder;
import com.sap.sse.datamining.DataRetrieverChainDefinition;
import com.sap.sse.datamining.components.FilterCriterion;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.criterias.AbstractFilterCriterion;
import com.sap.sse.datamining.test.functions.registry.test_classes.Test_Regatta;
import com.sap.sse.datamining.test.functions.registry.test_contexts.Test_HasLegOfCompetitorContext;
import com.sap.sse.datamining.test.functions.registry.test_contexts.Test_HasRaceContext;
import com.sap.sse.datamining.test.util.ComponentTestsUtil;
import com.sap.sse.datamining.test.util.ConcurrencyTestsUtil;
import com.sap.sse.datamining.test.util.components.NullProcessor;
import com.sap.sse.datamining.test.util.components.TestLegOfCompetitorWithContextRetrievalProcessor;
import com.sap.sse.datamining.test.util.components.TestRaceWithContextRetrievalProcessor;
import com.sap.sse.datamining.test.util.components.TestRegattaRetrievalProcessor;

public class TestDataRetrieverChainCreation {

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

    @SuppressWarnings("unchecked")
    @Before
    public void initializeRetrieverChain() {
        dataRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>((Class<Collection<Test_Regatta>>)(Class<?>) Collection.class, "TestRetrieverChain");
        Class<Processor<Collection<Test_Regatta>, Test_Regatta>> regattaRetrieverClass = (Class<Processor<Collection<Test_Regatta>, Test_Regatta>>)(Class<?>) TestRegattaRetrievalProcessor.class;
        dataRetrieverChainDefinition.startWith(regattaRetrieverClass, Test_Regatta.class);
        
        Class<Processor<Test_Regatta, Test_HasRaceContext>> raceRetrieverClass = 
                (Class<Processor<Test_Regatta, Test_HasRaceContext>>)(Class<?>) TestRaceWithContextRetrievalProcessor.class;
        dataRetrieverChainDefinition.addAsLast(regattaRetrieverClass,
                                               raceRetrieverClass,
                                               Test_HasRaceContext.class);
        
        Class<Processor<Test_HasRaceContext, Test_HasLegOfCompetitorContext>> legRetrieverClass = 
                (Class<Processor<Test_HasRaceContext, Test_HasLegOfCompetitorContext>>)(Class<?>) TestLegOfCompetitorWithContextRetrievalProcessor.class;
        dataRetrieverChainDefinition.addAsLast(raceRetrieverClass,
                                               legRetrieverClass,
                                               Test_HasLegOfCompetitorContext.class);
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
    
    @SuppressWarnings("unchecked")
    @Test
    public void testCreationWithExistingChainDefinition() {
        DataRetrieverChainDefinition<Collection<Test_Regatta>> chainClone = new SimpleDataRetrieverChainDefinition<>(dataRetrieverChainDefinition, "TestRetrieverChain");
        assertThat(chainClone.getDataSourceType().equals(dataRetrieverChainDefinition.getDataSourceType()), is(true));
        assertThat(chainClone.getRetrievedDataType().equals(dataRetrieverChainDefinition.getRetrievedDataType()), is(true));
        
        List<DataRetrieverTypeWithInformation<?, ?>> cloneDataRetrieverTypesWithInformation = (List<DataRetrieverTypeWithInformation<?, ?>>) chainClone.getDataRetrieverTypesWithInformation();
        assertThat(cloneDataRetrieverTypesWithInformation, is(dataRetrieverChainDefinition.getDataRetrieverTypesWithInformation()));
        
        assertThat(chainClone.getUUID(), not(dataRetrieverChainDefinition.getUUID()));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetRetrievedDataTypeAndGetDataSourceType() {
        DataRetrieverChainDefinition<Collection<Test_Regatta>> dataRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>((Class<Collection<Test_Regatta>>)(Class<?>) Collection.class, "TestRetrieverChain");
        assertThat(dataRetrieverChainDefinition.getDataSourceType().equals(Collection.class), is(true));
        
        Class<Processor<Collection<Test_Regatta>, Test_Regatta>> regattaRetrieverClass = (Class<Processor<Collection<Test_Regatta>, Test_Regatta>>)(Class<?>) TestRegattaRetrievalProcessor.class;
        dataRetrieverChainDefinition.startWith(regattaRetrieverClass, Test_Regatta.class);
        assertThat(dataRetrieverChainDefinition.getRetrievedDataType().equals(Test_Regatta.class), is(true));
        
        Class<Processor<Test_Regatta, Test_HasRaceContext>> raceRetrieverClass = 
                (Class<Processor<Test_Regatta, Test_HasRaceContext>>)(Class<?>) TestRaceWithContextRetrievalProcessor.class;
        dataRetrieverChainDefinition.addAsLast(regattaRetrieverClass,
                                               raceRetrieverClass,
                                               Test_HasRaceContext.class);
        assertThat(dataRetrieverChainDefinition.getRetrievedDataType().equals(Test_HasRaceContext.class), is(true));
        
        Class<Processor<Test_HasRaceContext, Test_HasLegOfCompetitorContext>> legRetrieverClass = 
                (Class<Processor<Test_HasRaceContext, Test_HasLegOfCompetitorContext>>)(Class<?>) TestLegOfCompetitorWithContextRetrievalProcessor.class;
        dataRetrieverChainDefinition.addAsLast(raceRetrieverClass,
                                               legRetrieverClass,
                                               Test_HasLegOfCompetitorContext.class);
        assertThat(dataRetrieverChainDefinition.getRetrievedDataType().equals(Test_HasLegOfCompetitorContext.class), is(true));
    }

    @Test
    public void testStepByStepDataRetrieverChainCreation() throws InterruptedException {
        DataRetrieverChainBuilder<Collection<Test_Regatta>> chainBuilder = dataRetrieverChainDefinition.startBuilding(ConcurrencyTestsUtil.getExecutor());
        assertThat(chainBuilder.getCurrentRetrievedDataType().equals(Test_Regatta.class), is(true));
        assertThat(chainBuilder.canStepDeeper(), is(true));
        
        chainBuilder.stepDeeper().setFilter(raceFilter);
        assertThat(chainBuilder.getCurrentRetrievedDataType().equals(Test_HasRaceContext.class), is(true));
        assertThat(chainBuilder.canStepDeeper(), is(true));
        
        chainBuilder.stepDeeper().setFilter(legOfCompetitorFilter).addResultReceiver(legReceiver);
        assertThat(chainBuilder.getCurrentRetrievedDataType().equals(Test_HasLegOfCompetitorContext.class), is(true));
        assertThat(chainBuilder.canStepDeeper(), is(false));
        
        Processor<Collection<Test_Regatta>, ?> firstRetrieverInChain = chainBuilder.build();
        firstRetrieverInChain.processElement(ComponentTestsUtil.createExampleDataSource());
        firstRetrieverInChain.finish();
        ConcurrencyTestsUtil.sleepFor(100);
        assertThat(receivedLegOfCompetitorAmount, is(greaterThan(0)));
        assertThat(receivedLegOfCompetitorAmount, is(lessThanOrEqualTo(3 /*races*/ * 3 /*legs*/ * 4 /*competitors*/)));
    }
    
    @SuppressWarnings("unchecked")
    @Test(expected=UnsupportedOperationException.class)
    public void testAddAsLastWithoutStarting() {
        DataRetrieverChainDefinition<Collection<Test_Regatta>> dataRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>((Class<Collection<Test_Regatta>>)(Class<?>) Collection.class, "TestRetrieverChain");

        Class<Processor<Collection<Test_Regatta>, Test_Regatta>> regattaRetrieverClass = (Class<Processor<Collection<Test_Regatta>, Test_Regatta>>)(Class<?>) TestRegattaRetrievalProcessor.class;
        Class<Processor<Test_Regatta, Test_HasRaceContext>> raceRetrieverClass = 
                (Class<Processor<Test_Regatta, Test_HasRaceContext>>)(Class<?>) TestRaceWithContextRetrievalProcessor.class;
        dataRetrieverChainDefinition.addAsLast(regattaRetrieverClass,
                                               raceRetrieverClass,
                                               Test_HasRaceContext.class);
    }
    
    @SuppressWarnings("unchecked")
    @Test(expected=UnsupportedOperationException.class)
    public void testStartingTwice() {
        DataRetrieverChainDefinition<Collection<Test_Regatta>> dataRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>((Class<Collection<Test_Regatta>>)(Class<?>) Collection.class, "TestRetrieverChain");
        Class<Processor<Collection<Test_Regatta>, Test_Regatta>> regattaRetrieverClass = (Class<Processor<Collection<Test_Regatta>, Test_Regatta>>)(Class<?>) TestRegattaRetrievalProcessor.class;
        dataRetrieverChainDefinition.startWith(regattaRetrieverClass, Test_Regatta.class);
        dataRetrieverChainDefinition.startWith(regattaRetrieverClass, Test_Regatta.class);
    }
    
    @SuppressWarnings("unchecked")
    @Test(expected=IllegalArgumentException.class)
    public void testStartingWithProcessorWithoutUsableConstructor() {
        DataRetrieverChainDefinition<Collection<Test_Regatta>> dataRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>((Class<Collection<Test_Regatta>>)(Class<?>) Collection.class, "TestRetrieverChain");
        dataRetrieverChainDefinition.startWith((Class<Processor<Collection<Test_Regatta>, Test_Regatta>>)(Class<?>) Processor.class, Test_Regatta.class);
    }
    
    @SuppressWarnings("unchecked")
    @Test(expected=IllegalArgumentException.class)
    public void testAddingProcessorWithoutUsableConstructor() {
        DataRetrieverChainDefinition<Collection<Test_Regatta>> dataRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>((Class<Collection<Test_Regatta>>)(Class<?>) Collection.class, "TestRetrieverChain");
        Class<Processor<Collection<Test_Regatta>, Test_Regatta>> regattaRetrieverClass = (Class<Processor<Collection<Test_Regatta>, Test_Regatta>>)(Class<?>) TestRegattaRetrievalProcessor.class;
        dataRetrieverChainDefinition.startWith(regattaRetrieverClass, Test_Regatta.class);
        
        dataRetrieverChainDefinition.addAsLast(regattaRetrieverClass,
                                               (Class<Processor<Test_Regatta, Test_HasRaceContext>>)(Class<?>) Processor.class,
                                               Test_HasRaceContext.class);
    }
    
    @SuppressWarnings("unchecked")
    @Test(expected=IllegalArgumentException.class)
    public void testAddingRetrieverThatDoesntMatch() {
        DataRetrieverChainDefinition<Collection<Test_Regatta>> dataRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>((Class<Collection<Test_Regatta>>)(Class<?>) Collection.class, "TestRetrieverChain");
        Class<Processor<Collection<Test_Regatta>, Test_Regatta>> regattaRetrieverClass = (Class<Processor<Collection<Test_Regatta>, Test_Regatta>>)(Class<?>) TestRegattaRetrievalProcessor.class;
        dataRetrieverChainDefinition.startWith(regattaRetrieverClass, Test_Regatta.class);
        
        Class<Processor<Test_Regatta, Test_HasRaceContext>> raceRetrieverClass = 
                (Class<Processor<Test_Regatta, Test_HasRaceContext>>)(Class<?>) TestRaceWithContextRetrievalProcessor.class;
        Class<Processor<Test_HasRaceContext, Test_HasLegOfCompetitorContext>> legRetrieverClass = 
                (Class<Processor<Test_HasRaceContext, Test_HasLegOfCompetitorContext>>)(Class<?>) TestLegOfCompetitorWithContextRetrievalProcessor.class;
        dataRetrieverChainDefinition.addAsLast(raceRetrieverClass,
                                               legRetrieverClass,
                                               Test_HasLegOfCompetitorContext.class);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testSettingAFilterWithWrongElementType() {
        dataRetrieverChainDefinition.startBuilding(ConcurrencyTestsUtil.getExecutor()).setFilter(raceFilter);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testSettingAResultReceiverWithWrongInputType() {
        dataRetrieverChainDefinition.startBuilding(ConcurrencyTestsUtil.getExecutor()).addResultReceiver(legReceiver);
    }

}
