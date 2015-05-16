package com.sap.sse.datamining.impl.components.management;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.datamining.ModifiableDataMiningServer;
import com.sap.sse.datamining.components.DataRetrieverChainDefinition;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.SimpleDataRetrieverChainDefinition;
import com.sap.sse.datamining.test.functions.registry.test_classes.Test_Regatta;
import com.sap.sse.datamining.test.functions.registry.test_contexts.Test_HasLegOfCompetitorContext;
import com.sap.sse.datamining.test.functions.registry.test_contexts.Test_HasRaceContext;
import com.sap.sse.datamining.test.functions.registry.test_contexts.Test_HasRaceContextImpl;
import com.sap.sse.datamining.test.util.ConcurrencyTestsUtil;
import com.sap.sse.datamining.test.util.TestsUtil;
import com.sap.sse.datamining.test.util.components.TestLegOfCompetitorWithContextRetrievalProcessor;
import com.sap.sse.datamining.test.util.components.TestRaceWithContextRetrievalProcessor;
import com.sap.sse.datamining.test.util.components.TestRegattaRetrievalProcessor;

public class TestDataRetrieverChainDefinitionManagement {
    
    private DataRetrieverChainDefinition<Collection<Test_Regatta>, Test_HasRaceContext> raceRetrieverChainDefinition;
    private DataRetrieverChainDefinition<Collection<Test_Regatta>, Test_HasLegOfCompetitorContext> legRetrieverChainDefinition;
    private ModifiableDataMiningServer server;

    @SuppressWarnings("unchecked")
    @Before
    public void initializeRetrieverChainsAndRegistry() {
        raceRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>((Class<Collection<Test_Regatta>>)(Class<?>) Collection.class, Test_HasRaceContext.class, "TestRaceRetrieverChain");
        Class<Processor<Collection<Test_Regatta>, Test_Regatta>> regattaRetrieverClass = (Class<Processor<Collection<Test_Regatta>, Test_Regatta>>)(Class<?>) TestRegattaRetrievalProcessor.class;
        raceRetrieverChainDefinition.startWith(regattaRetrieverClass, Test_Regatta.class, "regatta");
        
        Class<Processor<Test_Regatta, Test_HasRaceContext>> raceRetrieverClass = 
                (Class<Processor<Test_Regatta, Test_HasRaceContext>>)(Class<?>) TestRaceWithContextRetrievalProcessor.class;
        raceRetrieverChainDefinition.endWith(regattaRetrieverClass,
                                               raceRetrieverClass,
                                               Test_HasRaceContext.class, "race");

        legRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>(raceRetrieverChainDefinition, Test_HasLegOfCompetitorContext.class, "TestLegRetrieverChain");
        
        Class<Processor<Test_HasRaceContext, Test_HasLegOfCompetitorContext>> legRetrieverClass = 
                (Class<Processor<Test_HasRaceContext, Test_HasLegOfCompetitorContext>>)(Class<?>) TestLegOfCompetitorWithContextRetrievalProcessor.class;
        legRetrieverChainDefinition.endWith(raceRetrieverClass,
                                               legRetrieverClass,
                                               Test_HasLegOfCompetitorContext.class, "legOfCompetitor");
        
        server = TestsUtil.createNewServer();
        server.registerDataRetrieverChainDefinition(raceRetrieverChainDefinition);
        server.registerDataRetrieverChainDefinition(legRetrieverChainDefinition);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetDataRetrieverChainDefinitions() {
        Class<Collection<Test_Regatta>> dataSourceType = (Class<Collection<Test_Regatta>>)(Class<?>) Collection.class;
        
        Collection<DataRetrieverChainDefinition<?, ?>> expectedRetrieverChainDefinitions = new HashSet<>();
        expectedRetrieverChainDefinitions.add(raceRetrieverChainDefinition);
        assertThat(server.getDataRetrieverChainDefinitions(dataSourceType, Test_HasRaceContext.class), is(expectedRetrieverChainDefinitions));
        
        expectedRetrieverChainDefinitions = new HashSet<>();
        expectedRetrieverChainDefinitions.add(legRetrieverChainDefinition);
        assertThat(server.getDataRetrieverChainDefinitions(dataSourceType, Test_HasLegOfCompetitorContext.class), is(expectedRetrieverChainDefinitions));
        
        expectedRetrieverChainDefinitions = new HashSet<>();
        expectedRetrieverChainDefinitions.add(raceRetrieverChainDefinition);
        expectedRetrieverChainDefinitions.add(legRetrieverChainDefinition);
        assertThat(server.getDataRetrieverChainDefinitionsBySourceType(dataSourceType), is(expectedRetrieverChainDefinitions));
        
        expectedRetrieverChainDefinitions = new HashSet<>();
        expectedRetrieverChainDefinitions.add(legRetrieverChainDefinition);
        assertThat(server.getDataRetrieverChainDefinitionsByDataType(Test_HasLegOfCompetitorContext.class), is(expectedRetrieverChainDefinitions));
        
        expectedRetrieverChainDefinitions = new HashSet<>();
        expectedRetrieverChainDefinitions.add(raceRetrieverChainDefinition);
        assertThat(server.getDataRetrieverChainDefinitionsByDataType(Test_HasRaceContext.class), is(expectedRetrieverChainDefinitions));
    }
    
    @Test
    public void testSingleGetDataRetrieverChainDefinition() {
        assertThat(server.getDataRetrieverChainDefinition(raceRetrieverChainDefinition.getID()),
                is(raceRetrieverChainDefinition));
        
        assertThat(server.getDataRetrieverChainDefinition(legRetrieverChainDefinition.getID()),
                is(legRetrieverChainDefinition));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testRemoveDataRetrieverChainDefinition() {
        Class<Collection<Test_Regatta>> dataSourceType = (Class<Collection<Test_Regatta>>)(Class<?>) Collection.class;

        Date beforeUnregistration = new Date();
        ConcurrencyTestsUtil.sleepFor(10);
        server.unregisterDataRetrieverChainDefinition(legRetrieverChainDefinition);
        assertThat(server.getComponentsChangedTimepoint().after(beforeUnregistration), is(true));
        
        beforeUnregistration = new Date();
        ConcurrencyTestsUtil.sleepFor(10);
        server.unregisterDataRetrieverChainDefinition(legRetrieverChainDefinition);
        assertThat(server.getComponentsChangedTimepoint().after(beforeUnregistration), is(false));
        
        Collection<DataRetrieverChainDefinition<Collection<Test_Regatta>, Test_HasLegOfCompetitorContext>> expectedEmptyDataRetrieverChainDefinitions = new HashSet<>();
        assertThat(server.getDataRetrieverChainDefinitions(dataSourceType, Test_HasLegOfCompetitorContext.class), is(expectedEmptyDataRetrieverChainDefinitions));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testAddingDataRetrieverChainDefinitionTwice() {
        Class<Collection<Test_Regatta>> dataSourceType = (Class<Collection<Test_Regatta>>)(Class<?>) Collection.class;

        Date beforeRegistration = new Date();
        ConcurrencyTestsUtil.sleepFor(10);
        server.registerDataRetrieverChainDefinition(legRetrieverChainDefinition);
        assertThat(server.getComponentsChangedTimepoint().after(beforeRegistration), is(false));
        
        Collection<DataRetrieverChainDefinition<Collection<Test_Regatta>, Test_HasLegOfCompetitorContext>> expectedDataRetrieverChainDefinitions = new HashSet<>();
        expectedDataRetrieverChainDefinitions.add(legRetrieverChainDefinition);
        assertThat(server.getDataRetrieverChainDefinitions(dataSourceType, Test_HasLegOfCompetitorContext.class), is(expectedDataRetrieverChainDefinitions));
    }

    @Test
    public void testGetDataRetrieverChainDefinitionsForNotRegisteredChain() {
        Collection<DataRetrieverChainDefinition<Test_Regatta, Test_HasRaceContextImpl>> expectedEmptyDataRetrieverChainDefinitions = new HashSet<>();
        assertThat(server.getDataRetrieverChainDefinitions(Test_Regatta.class, Test_HasRaceContextImpl.class), is(expectedEmptyDataRetrieverChainDefinitions));
    }

}
