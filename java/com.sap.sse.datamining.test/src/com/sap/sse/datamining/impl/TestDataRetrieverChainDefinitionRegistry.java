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
        raceRetrieverChainDefinition.startWith(regattaRetrieverClass, Test_Regatta.class);
        
        Class<Processor<Test_Regatta, Test_HasRaceContext>> raceRetrieverClass = 
                (Class<Processor<Test_Regatta, Test_HasRaceContext>>)(Class<?>) TestRaceWithContextRetrievalProcessor.class;
        raceRetrieverChainDefinition.addAsLast(regattaRetrieverClass,
                                               raceRetrieverClass,
                                               Test_HasRaceContext.class);

        legRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<Collection<Test_Regatta>>(raceRetrieverChainDefinition, "TestLegRetrieverChain");
        
        Class<Processor<Test_HasRaceContext, Test_HasLegOfCompetitorContext>> legRetrieverClass = 
                (Class<Processor<Test_HasRaceContext, Test_HasLegOfCompetitorContext>>)(Class<?>) TestLegOfCompetitorWithContextRetrievalProcessor.class;
        legRetrieverChainDefinition.addAsLast(raceRetrieverClass,
                                               legRetrieverClass,
                                               Test_HasLegOfCompetitorContext.class);
        
        dataRetrieverChainDefinitionRegistry = new SimpleDataRetrieverChainDefinitionRegistry();
        dataRetrieverChainDefinitionRegistry.add(raceRetrieverChainDefinition);
        dataRetrieverChainDefinitionRegistry.add(legRetrieverChainDefinition);
        
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetDataRetrieverChainDefinitions() {
        Class<Collection<Test_Regatta>> dataSourceType = (Class<Collection<Test_Regatta>>)(Class<?>) Collection.class;
        
        Collection<DataRetrieverChainDefinition<Collection<Test_Regatta>>> expectedDataRetrieverChainDefinitions = new HashSet<>();
        expectedDataRetrieverChainDefinitions.add(raceRetrieverChainDefinition);
        assertThat(dataRetrieverChainDefinitionRegistry.getDataRetrieverChainDefinitions(dataSourceType, Test_HasRaceContext.class), is(expectedDataRetrieverChainDefinitions));
        
        expectedDataRetrieverChainDefinitions = new HashSet<>();
        expectedDataRetrieverChainDefinitions.add(legRetrieverChainDefinition);
        assertThat(dataRetrieverChainDefinitionRegistry.getDataRetrieverChainDefinitions(dataSourceType, Test_HasLegOfCompetitorContext.class), is(expectedDataRetrieverChainDefinitions));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testSingleGetDataRetrieverChainDefinition() {
        Class<Collection<Test_Regatta>> dataSourceType = (Class<Collection<Test_Regatta>>)(Class<?>) Collection.class;
        
        assertThat(dataRetrieverChainDefinitionRegistry.getDataRetrieverChainDefinition(dataSourceType, raceRetrieverChainDefinition.getUUID()),
                is(raceRetrieverChainDefinition));
        
        assertThat(dataRetrieverChainDefinitionRegistry.getDataRetrieverChainDefinition(dataSourceType, legRetrieverChainDefinition.getUUID()),
                is(legRetrieverChainDefinition));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testRemoveDataRetrieverChainDefinition() {
        Class<Collection<Test_Regatta>> dataSourceType = (Class<Collection<Test_Regatta>>)(Class<?>) Collection.class;

        dataRetrieverChainDefinitionRegistry.remove(legRetrieverChainDefinition);
        Collection<DataRetrieverChainDefinition<Collection<Test_Regatta>>> expectedEmptyDataRetrieverChainDefinitions = new HashSet<>();
        assertThat(dataRetrieverChainDefinitionRegistry.getDataRetrieverChainDefinitions(dataSourceType, Test_HasLegOfCompetitorContext.class), is(expectedEmptyDataRetrieverChainDefinitions));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testAddingDataRetrieverChainDefinitionTwice() {
        Class<Collection<Test_Regatta>> dataSourceType = (Class<Collection<Test_Regatta>>)(Class<?>) Collection.class;

        dataRetrieverChainDefinitionRegistry.add(legRetrieverChainDefinition);
        Collection<DataRetrieverChainDefinition<Collection<Test_Regatta>>> expectedDataRetrieverChainDefinitions = new HashSet<>();
        expectedDataRetrieverChainDefinitions.add(legRetrieverChainDefinition);
        assertThat(dataRetrieverChainDefinitionRegistry.getDataRetrieverChainDefinitions(dataSourceType, Test_HasLegOfCompetitorContext.class), is(expectedDataRetrieverChainDefinitions));
    }

    @Test
    public void testGetDataRetrieverChainDefinitionsForNotRegisteredChain() {
        Collection<DataRetrieverChainDefinition<Test_Regatta>> expectedEmptyDataRetrieverChainDefinitions = new HashSet<DataRetrieverChainDefinition<Test_Regatta>>();
        assertThat(dataRetrieverChainDefinitionRegistry.getDataRetrieverChainDefinitions(Test_Regatta.class, Test_HasRaceContextImpl.class), is(expectedEmptyDataRetrieverChainDefinitions));
    }

}
