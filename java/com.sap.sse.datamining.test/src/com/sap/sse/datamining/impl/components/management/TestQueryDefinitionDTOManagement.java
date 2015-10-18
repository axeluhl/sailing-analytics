package com.sap.sse.datamining.impl.components.management;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import org.junit.BeforeClass;
import org.junit.Test;

import com.sap.sse.datamining.ModifiableDataMiningServer;
import com.sap.sse.datamining.exceptions.DataMiningComponentAlreadyRegisteredForKeyException;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.AggregationProcessorDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.dto.ModifiableStatisticQueryDefinitionDTO;
import com.sap.sse.datamining.test.util.ConcurrencyTestsUtil;
import com.sap.sse.datamining.test.util.TestsUtil;

public class TestQueryDefinitionDTOManagement {

    private static StatisticQueryDefinitionDTO pseudoQueryDefinition;
    private static StatisticQueryDefinitionDTO differentQueryDefinition;

    @BeforeClass
    public static void initializeQueryDefinitions() {
        FunctionDTO statisticToCalculate = new FunctionDTO(false, "Test", "Test", "Test", new ArrayList<String>(), "Test", 0);
        AggregationProcessorDefinitionDTO aggregatorDefinition = new AggregationProcessorDefinitionDTO("Test", "Test", "Test", "Test");
        DataRetrieverChainDefinitionDTO dataRetrieverChainDefinition = new DataRetrieverChainDefinitionDTO("Test", "Test", new ArrayList<DataRetrieverLevelDTO>());
        pseudoQueryDefinition = new ModifiableStatisticQueryDefinitionDTO("default", statisticToCalculate, aggregatorDefinition, dataRetrieverChainDefinition);

        statisticToCalculate = new FunctionDTO(false, "Different", "Different", "Different", new ArrayList<String>(), "Different", 0);
        aggregatorDefinition = new AggregationProcessorDefinitionDTO("Different", "Different", "Different", "Different");
        dataRetrieverChainDefinition = new DataRetrieverChainDefinitionDTO("Different", "Different", new ArrayList<DataRetrieverLevelDTO>());
        differentQueryDefinition = new ModifiableStatisticQueryDefinitionDTO("default", statisticToCalculate, aggregatorDefinition, dataRetrieverChainDefinition);
    }

    @Test
    public void testQueryDefinitionRegistration() {
        ModifiableDataMiningServer server = TestsUtil.createNewServer();
        
        Date beforeRegistration = new Date();
        ConcurrencyTestsUtil.sleepFor(10);
        server.registerPredefinedQueryDefinition("Test", pseudoQueryDefinition);
        assertThat(server.getComponentsChangedTimepoint().after(beforeRegistration), is(true));
        
        beforeRegistration = new Date();
        ConcurrencyTestsUtil.sleepFor(10);
        server.registerPredefinedQueryDefinition("Test", pseudoQueryDefinition);
        assertThat(server.getComponentsChangedTimepoint().after(beforeRegistration), is(false));
    }
    
    @Test(expected=DataMiningComponentAlreadyRegisteredForKeyException.class)
    public void testQueryDefinitionRegistrationWithConflict() {
        ModifiableDataMiningServer server = TestsUtil.createNewServer();
        server.registerPredefinedQueryDefinition("Test", pseudoQueryDefinition);
        server.registerPredefinedQueryDefinition("Test", differentQueryDefinition);
    }

    @Test
    public void testQueryDefinitionUnregistration() {
        ModifiableDataMiningServer server = TestsUtil.createNewServer();
        server.registerPredefinedQueryDefinition("Test", pseudoQueryDefinition);
        
        Date beforeRegistration = new Date();
        ConcurrencyTestsUtil.sleepFor(10);
        server.unregisterPredefinedQueryDefinition("Test", pseudoQueryDefinition);
        assertThat(server.getComponentsChangedTimepoint().after(beforeRegistration), is(true));
        
        beforeRegistration = new Date();
        ConcurrencyTestsUtil.sleepFor(10);
        server.unregisterPredefinedQueryDefinition("Test", pseudoQueryDefinition);
        assertThat(server.getComponentsChangedTimepoint().after(beforeRegistration), is(false));
    }
    
    @Test
    public void getByName() {
        ModifiableDataMiningServer server = TestsUtil.createNewServer();
        server.registerPredefinedQueryDefinition("Test", pseudoQueryDefinition);
        server.registerPredefinedQueryDefinition("Different", differentQueryDefinition);

        assertThat(server.getQueryDefinitionDTOProvider().get("Test"), is(pseudoQueryDefinition));
        assertThat(server.getQueryDefinitionDTOProvider().get("Different"), is(differentQueryDefinition));
        
        Collection<String> predefinedQueryNames = new HashSet<>();
        predefinedQueryNames.add("Test");
        predefinedQueryNames.add("Different");
        assertThat(server.getPredefinedQueryNames(), is(predefinedQueryNames));
    }

}
