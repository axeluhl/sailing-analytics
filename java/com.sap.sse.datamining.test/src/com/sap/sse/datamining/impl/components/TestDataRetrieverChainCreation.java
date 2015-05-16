package com.sap.sse.datamining.impl.components;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.datamining.components.DataRetrieverChainBuilder;
import com.sap.sse.datamining.components.DataRetrieverChainDefinition;
import com.sap.sse.datamining.components.FilterCriterion;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.DataRetrieverTypeWithInformation;
import com.sap.sse.datamining.impl.components.SimpleDataRetrieverChainDefinition;
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

    private DataRetrieverChainDefinition<Collection<Test_Regatta>, Test_HasLegOfCompetitorContext> dataRetrieverChainDefinition;
    
    private FilterCriterion<Test_HasRaceContext> raceFilter;
    private FilterCriterion<Test_HasLegOfCompetitorContext> legOfCompetitorFilter;
    
    private int receivedLegOfCompetitorAmount = 0;
    private Processor<Test_HasLegOfCompetitorContext, Void> legReceiver = new NullProcessor<Test_HasLegOfCompetitorContext, Void>(Test_HasLegOfCompetitorContext.class, Void.class) {
        @Override
        public void processElement(Test_HasLegOfCompetitorContext element) {
            synchronized (this) {
                receivedLegOfCompetitorAmount++;
            }
        }
    };

    @SuppressWarnings("unchecked")
    @Before
    public void initializeRetrieverChain() {
        dataRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>((Class<Collection<Test_Regatta>>)(Class<?>) Collection.class, Test_HasLegOfCompetitorContext.class, "TestRetrieverChain");
        dataRetrieverChainDefinition.startWith(TestRegattaRetrievalProcessor.class, Test_Regatta.class, "regatta");
        
        dataRetrieverChainDefinition.addAfter(TestRegattaRetrievalProcessor.class,
                                              TestRaceWithContextRetrievalProcessor.class,
                                              Test_HasRaceContext.class, "race");
        
        dataRetrieverChainDefinition.endWith(TestRaceWithContextRetrievalProcessor.class,
                                             TestLegOfCompetitorWithContextRetrievalProcessor.class,
                                             Test_HasLegOfCompetitorContext.class, "legOfCompetitor");
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
    @Test(expected=IllegalStateException.class)
    public void testCreationWithExistingChainDefinition() {
        DataRetrieverChainDefinition<Collection<Test_Regatta>, ?> chainClone = new SimpleDataRetrieverChainDefinition<>(dataRetrieverChainDefinition, Void.class, "TestRetrieverChain");
        assertThat(chainClone.getDataSourceType().equals(dataRetrieverChainDefinition.getDataSourceType()), is(true));
        
        List<DataRetrieverTypeWithInformation<?, ?>> cloneDataRetrieverTypesWithInformation = (List<DataRetrieverTypeWithInformation<?, ?>>) chainClone.getDataRetrieverTypesWithInformation();
        assertThat(cloneDataRetrieverTypesWithInformation, is(dataRetrieverChainDefinition.getDataRetrieverTypesWithInformation()));
        
        assertThat(chainClone.getID(), not(dataRetrieverChainDefinition.getID()));
        
        chainClone.startBuilding(ConcurrencyTestsUtil.getExecutor());
    }
    
    @Test
    public void testThatTheDataRetrieverChainBuilderHasToBeInitialized() {
        DataRetrieverChainBuilder<Collection<Test_Regatta>> chainBuilder = dataRetrieverChainDefinition.startBuilding(ConcurrencyTestsUtil.getExecutor());

        try {
            chainBuilder.getCurrentRetrievedDataType();
        } catch (IllegalStateException e) {
        }
        try {
            chainBuilder.setFilter(null);
        } catch (IllegalStateException e) {
        }
        try {
            chainBuilder.addResultReceiver(null);
        } catch (IllegalStateException e) {
        }
        try {
            chainBuilder.build();
        } catch (IllegalStateException e) {
        }
    }

    @Test
    public void testStepByStepDataRetrieverChainCreation() throws InterruptedException {
        DataRetrieverChainBuilder<Collection<Test_Regatta>> chainBuilder = dataRetrieverChainDefinition.startBuilding(ConcurrencyTestsUtil.getExecutor());
        chainBuilder.stepFurther(); //Initialization
        
        assertThat(chainBuilder.getCurrentRetrievedDataType().equals(Test_Regatta.class), is(true));
        assertThat(chainBuilder.canStepFurther(), is(true));
        
        chainBuilder.stepFurther().setFilter(raceFilter);
        assertThat(chainBuilder.getCurrentRetrievedDataType().equals(Test_HasRaceContext.class), is(true));
        assertThat(chainBuilder.canStepFurther(), is(true));
        
        chainBuilder.stepFurther().setFilter(legOfCompetitorFilter).addResultReceiver(legReceiver);
        assertThat(chainBuilder.getCurrentRetrievedDataType().equals(Test_HasLegOfCompetitorContext.class), is(true));
        assertThat(chainBuilder.canStepFurther(), is(false));
        
        Processor<Collection<Test_Regatta>, ?> firstRetrieverInChain = chainBuilder.build();
        firstRetrieverInChain.processElement(ComponentTestsUtil.createExampleDataSource());
        firstRetrieverInChain.finish();
        ConcurrencyTestsUtil.sleepFor(100);
        assertThat(receivedLegOfCompetitorAmount, is(3 /*races*/ * 3 /*legs*/ * 4 /*competitors*/));
    }
    
    @SuppressWarnings("unchecked")
    @Test(expected=IllegalStateException.class)
    public void testAddAfterWithoutStarting() {
        DataRetrieverChainDefinition<Collection<Test_Regatta>, ?> dataRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>((Class<Collection<Test_Regatta>>)(Class<?>) Collection.class, Void.class, "TestRetrieverChain");

        Class<Processor<Collection<Test_Regatta>, Test_Regatta>> regattaRetrieverClass = (Class<Processor<Collection<Test_Regatta>, Test_Regatta>>)(Class<?>) TestRegattaRetrievalProcessor.class;
        Class<Processor<Test_Regatta, Test_HasRaceContext>> raceRetrieverClass = 
                (Class<Processor<Test_Regatta, Test_HasRaceContext>>)(Class<?>) TestRaceWithContextRetrievalProcessor.class;
        dataRetrieverChainDefinition.addAfter(regattaRetrieverClass,
                                               raceRetrieverClass,
                                               Test_HasRaceContext.class, "race");
    }
    
    @SuppressWarnings("unchecked")
    @Test(expected=IllegalStateException.class)
    public void testStartingTwice() {
        DataRetrieverChainDefinition<Collection<Test_Regatta>, ?> dataRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>((Class<Collection<Test_Regatta>>)(Class<?>) Collection.class, Void.class, "TestRetrieverChain");
        Class<Processor<Collection<Test_Regatta>, Test_Regatta>> regattaRetrieverClass = (Class<Processor<Collection<Test_Regatta>, Test_Regatta>>)(Class<?>) TestRegattaRetrievalProcessor.class;
        dataRetrieverChainDefinition.startWith(regattaRetrieverClass, Test_Regatta.class, "regatta");
        dataRetrieverChainDefinition.startWith(regattaRetrieverClass, Test_Regatta.class, "regatta");
    }
    
    @SuppressWarnings("unchecked")
    @Test(expected=IllegalArgumentException.class)
    public void testStartingWithProcessorWithoutUsableConstructor() {
        DataRetrieverChainDefinition<Collection<Test_Regatta>, ?> dataRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>((Class<Collection<Test_Regatta>>)(Class<?>) Collection.class, Void.class, "TestRetrieverChain");
        dataRetrieverChainDefinition.startWith((Class<Processor<Collection<Test_Regatta>, Test_Regatta>>)(Class<?>) Processor.class, Test_Regatta.class, "regatta");
    }
    
    @SuppressWarnings("unchecked")
    @Test(expected=IllegalArgumentException.class)
    public void testAddingProcessorWithoutUsableConstructor() {
        DataRetrieverChainDefinition<Collection<Test_Regatta>, ?> dataRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>((Class<Collection<Test_Regatta>>)(Class<?>) Collection.class, Void.class, "TestRetrieverChain");
        Class<Processor<Collection<Test_Regatta>, Test_Regatta>> regattaRetrieverClass = (Class<Processor<Collection<Test_Regatta>, Test_Regatta>>)(Class<?>) TestRegattaRetrievalProcessor.class;
        dataRetrieverChainDefinition.startWith(regattaRetrieverClass, Test_Regatta.class, "regatta");
        
        dataRetrieverChainDefinition.addAfter(regattaRetrieverClass,
                                              (Class<Processor<Test_Regatta, Test_HasRaceContext>>)(Class<?>) Processor.class,
                                              Test_HasRaceContext.class, "race");
    }
    
    @SuppressWarnings("unchecked")
    @Test(expected=IllegalArgumentException.class)
    public void testAddingRetrieverThatDoesntMatch() {
        DataRetrieverChainDefinition<Collection<Test_Regatta>, ?> dataRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>((Class<Collection<Test_Regatta>>)(Class<?>) Collection.class, Void.class, "TestRetrieverChain");
        Class<Processor<Collection<Test_Regatta>, Test_Regatta>> regattaRetrieverClass = (Class<Processor<Collection<Test_Regatta>, Test_Regatta>>)(Class<?>) TestRegattaRetrievalProcessor.class;
        dataRetrieverChainDefinition.startWith(regattaRetrieverClass, Test_Regatta.class, "regatta");
        
        Class<Processor<Test_Regatta, Test_HasRaceContext>> raceRetrieverClass = 
                (Class<Processor<Test_Regatta, Test_HasRaceContext>>)(Class<?>) TestRaceWithContextRetrievalProcessor.class;
        Class<Processor<Test_HasRaceContext, Test_HasLegOfCompetitorContext>> legRetrieverClass = 
                (Class<Processor<Test_HasRaceContext, Test_HasLegOfCompetitorContext>>)(Class<?>) TestLegOfCompetitorWithContextRetrievalProcessor.class;
        dataRetrieverChainDefinition.addAfter(raceRetrieverClass,
                                               legRetrieverClass,
                                               Test_HasLegOfCompetitorContext.class, "legOfCompetitor");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testSettingAFilterWithWrongElementType() {
        DataRetrieverChainBuilder<Collection<Test_Regatta>> chainBuilder = dataRetrieverChainDefinition.startBuilding(ConcurrencyTestsUtil.getExecutor());
        chainBuilder.stepFurther(); //Initialization
        
        chainBuilder.setFilter(raceFilter);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testSettingAResultReceiverWithWrongInputType() {
        DataRetrieverChainBuilder<Collection<Test_Regatta>> chainBuilder = dataRetrieverChainDefinition.startBuilding(ConcurrencyTestsUtil.getExecutor());
        chainBuilder.stepFurther(); //Initialization
        
        chainBuilder.addResultReceiver(legReceiver);
    }
    
    @SuppressWarnings("unchecked")
    @Test(expected=IllegalStateException.class)
    public void testEndingTwice() {
        Class<Processor<Test_Regatta, Test_HasRaceContext>> raceRetrieverClass = 
                (Class<Processor<Test_Regatta, Test_HasRaceContext>>)(Class<?>) TestRaceWithContextRetrievalProcessor.class;
        Class<Processor<Test_HasRaceContext, Test_HasLegOfCompetitorContext>> legRetrieverClass = 
                (Class<Processor<Test_HasRaceContext, Test_HasLegOfCompetitorContext>>)(Class<?>) TestLegOfCompetitorWithContextRetrievalProcessor.class;
        dataRetrieverChainDefinition.endWith(raceRetrieverClass, legRetrieverClass, Test_HasLegOfCompetitorContext.class, "legOfCompetitor");
    }
    
    @SuppressWarnings("unchecked")
    @Test(expected=IllegalStateException.class)
    public void testAddingRetrieverAfterCompletion() {
        Class<Processor<Test_Regatta, Test_HasRaceContext>> raceRetrieverClass = 
                (Class<Processor<Test_Regatta, Test_HasRaceContext>>)(Class<?>) TestRaceWithContextRetrievalProcessor.class;
        Class<Processor<Test_HasRaceContext, Test_HasLegOfCompetitorContext>> legRetrieverClass = 
                (Class<Processor<Test_HasRaceContext, Test_HasLegOfCompetitorContext>>)(Class<?>) TestLegOfCompetitorWithContextRetrievalProcessor.class;
        dataRetrieverChainDefinition.addAfter(raceRetrieverClass, legRetrieverClass, Test_HasLegOfCompetitorContext.class, "legOfCompetitor");
    }
    
    @SuppressWarnings("unchecked")
    @Test(expected=IllegalStateException.class)
    public void testStartBuildingWithIncompleteChain() {
        DataRetrieverChainDefinition<Collection<Test_Regatta>, Test_HasLegOfCompetitorContext> dataRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>((Class<Collection<Test_Regatta>>)(Class<?>) Collection.class, Test_HasLegOfCompetitorContext.class, "TestRetrieverChain");
        Class<Processor<Collection<Test_Regatta>, Test_Regatta>> regattaRetrieverClass = (Class<Processor<Collection<Test_Regatta>, Test_Regatta>>)(Class<?>) TestRegattaRetrievalProcessor.class;
        dataRetrieverChainDefinition.startWith(regattaRetrieverClass, Test_Regatta.class, "regatta");
        
        Class<Processor<Test_Regatta, Test_HasRaceContext>> raceRetrieverClass = 
                (Class<Processor<Test_Regatta, Test_HasRaceContext>>)(Class<?>) TestRaceWithContextRetrievalProcessor.class;
        dataRetrieverChainDefinition.addAfter(regattaRetrieverClass,
                                               raceRetrieverClass,
                                               Test_HasRaceContext.class, "race");
        
        dataRetrieverChainDefinition.startBuilding(ConcurrencyTestsUtil.getExecutor());
    }

}
