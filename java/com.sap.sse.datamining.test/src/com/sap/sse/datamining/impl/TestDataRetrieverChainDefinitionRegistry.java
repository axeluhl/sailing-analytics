package com.sap.sse.datamining.impl;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.datamining.DataRetrieverChainDefinition;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.test.functions.registry.test_classes.Test_Regatta;
import com.sap.sse.datamining.test.functions.registry.test_contexts.Test_HasLegOfCompetitorContext;
import com.sap.sse.datamining.test.functions.registry.test_contexts.Test_HasRaceContext;
import com.sap.sse.datamining.test.functions.registry.test_contexts.Test_HasRaceContextImpl;
import com.sap.sse.datamining.test.util.components.TestLegOfCompetitorWithContextRetrievalProcessor;
import com.sap.sse.datamining.test.util.components.TestRaceWithContextRetrievalProcessor;
import com.sap.sse.datamining.test.util.components.TestRegattaRetrievalProcessor;

public class TestDataRetrieverChainDefinitionRegistry {
    
    private DataRetrieverChainDefinition<Collection<Test_Regatta>> raceRetrieverChainDefinition;
    private DataRetrieverChainDefinition<Collection<Test_Regatta>> legRetrieverChainDefinition;
    private DataRetrieverChainDefinitionRegistry dataRetrieverChainDefinitionRegistry;

    @SuppressWarnings("unchecked")
    @Before
    public void initializeRetrieverChainsAndRegistry() {
        raceRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>((Class<Collection<Test_Regatta>>)(Class<?>) Collection.class, "TestRaceRetrieverChain");
        Class<Processor<Collection<Test_Regatta>, Test_Regatta>> regattaRetrieverClass = (Class<Processor<Collection<Test_Regatta>, Test_Regatta>>)(Class<?>) TestRegattaRetrievalProcessor.class;
        raceRetrieverChainDefinition.startWith(regattaRetrieverClass, Test_Regatta.class, "regatta");
        
        Class<Processor<Test_Regatta, Test_HasRaceContext>> raceRetrieverClass = 
                (Class<Processor<Test_Regatta, Test_HasRaceContext>>)(Class<?>) TestRaceWithContextRetrievalProcessor.class;
        raceRetrieverChainDefinition.addAfter(regattaRetrieverClass,
                                               raceRetrieverClass,
                                               Test_HasRaceContext.class, "race");

        legRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<Collection<Test_Regatta>>(raceRetrieverChainDefinition, "TestLegRetrieverChain");
        
        Class<Processor<Test_HasRaceContext, Test_HasLegOfCompetitorContext>> legRetrieverClass = 
                (Class<Processor<Test_HasRaceContext, Test_HasLegOfCompetitorContext>>)(Class<?>) TestLegOfCompetitorWithContextRetrievalProcessor.class;
        legRetrieverChainDefinition.addAfter(raceRetrieverClass,
                                               legRetrieverClass,
                                               Test_HasLegOfCompetitorContext.class, "legOfCompetitor");
        
        dataRetrieverChainDefinitionRegistry = new SimpleDataRetrieverChainDefinitionRegistry();
        dataRetrieverChainDefinitionRegistry.register(raceRetrieverChainDefinition);
        dataRetrieverChainDefinitionRegistry.register(legRetrieverChainDefinition);
        
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetDataRetrieverChainDefinitions() {
        Class<Collection<Test_Regatta>> dataSourceType = (Class<Collection<Test_Regatta>>)(Class<?>) Collection.class;
        
        Collection<DataRetrieverChainDefinition<Collection<Test_Regatta>>> expectedDataRetrieverChainDefinitions = new HashSet<>();
        expectedDataRetrieverChainDefinitions.add(raceRetrieverChainDefinition);
        assertThat(dataRetrieverChainDefinitionRegistry.get(dataSourceType, Test_HasRaceContext.class), is(expectedDataRetrieverChainDefinitions));
        
        expectedDataRetrieverChainDefinitions = new HashSet<>();
        expectedDataRetrieverChainDefinitions.add(legRetrieverChainDefinition);
        assertThat(dataRetrieverChainDefinitionRegistry.get(dataSourceType, Test_HasLegOfCompetitorContext.class), is(expectedDataRetrieverChainDefinitions));
    }
    
    @Test
    public void testSingleGetDataRetrieverChainDefinition() {
        assertThat(dataRetrieverChainDefinitionRegistry.get(raceRetrieverChainDefinition.getID()),
                is(raceRetrieverChainDefinition));
        
        assertThat(dataRetrieverChainDefinitionRegistry.get(legRetrieverChainDefinition.getID()),
                is(legRetrieverChainDefinition));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testRemoveDataRetrieverChainDefinition() {
        Class<Collection<Test_Regatta>> dataSourceType = (Class<Collection<Test_Regatta>>)(Class<?>) Collection.class;

        dataRetrieverChainDefinitionRegistry.unregister(legRetrieverChainDefinition);
        Collection<DataRetrieverChainDefinition<Collection<Test_Regatta>>> expectedEmptyDataRetrieverChainDefinitions = new HashSet<>();
        assertThat(dataRetrieverChainDefinitionRegistry.get(dataSourceType, Test_HasLegOfCompetitorContext.class), is(expectedEmptyDataRetrieverChainDefinitions));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testAddingDataRetrieverChainDefinitionTwice() {
        Class<Collection<Test_Regatta>> dataSourceType = (Class<Collection<Test_Regatta>>)(Class<?>) Collection.class;

        dataRetrieverChainDefinitionRegistry.register(legRetrieverChainDefinition);
        Collection<DataRetrieverChainDefinition<Collection<Test_Regatta>>> expectedDataRetrieverChainDefinitions = new HashSet<>();
        expectedDataRetrieverChainDefinitions.add(legRetrieverChainDefinition);
        assertThat(dataRetrieverChainDefinitionRegistry.get(dataSourceType, Test_HasLegOfCompetitorContext.class), is(expectedDataRetrieverChainDefinitions));
    }

    @Test
    public void testGetDataRetrieverChainDefinitionsForNotRegisteredChain() {
        Collection<DataRetrieverChainDefinition<Test_Regatta>> expectedEmptyDataRetrieverChainDefinitions = new HashSet<DataRetrieverChainDefinition<Test_Regatta>>();
        assertThat(dataRetrieverChainDefinitionRegistry.get(Test_Regatta.class, Test_HasRaceContextImpl.class), is(expectedEmptyDataRetrieverChainDefinitions));
    }

}
