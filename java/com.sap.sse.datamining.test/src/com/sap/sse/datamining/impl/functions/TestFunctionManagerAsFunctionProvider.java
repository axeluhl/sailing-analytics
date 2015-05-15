package com.sap.sse.datamining.impl.functions;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.datamining.ModifiableDataMiningServer;
import com.sap.sse.datamining.components.DataRetrieverChainDefinition;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.impl.components.SimpleDataRetrieverChainDefinition;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;
import com.sap.sse.datamining.test.functions.registry.test_classes.Test_Named;
import com.sap.sse.datamining.test.functions.registry.test_classes.Test_Regatta;
import com.sap.sse.datamining.test.functions.registry.test_contexts.Test_HasLegOfCompetitorContext;
import com.sap.sse.datamining.test.functions.registry.test_contexts.Test_HasLegOfCompetitorContextImpl;
import com.sap.sse.datamining.test.functions.registry.test_contexts.Test_HasRaceContext;
import com.sap.sse.datamining.test.functions.registry.test_contexts.Test_HasRaceContextImpl;
import com.sap.sse.datamining.test.functions.test_classes.SimpleClassWithMarkedMethods;
import com.sap.sse.datamining.test.functions.test_classes.Test_ExternalLibraryClass;
import com.sap.sse.datamining.test.util.ExpectedFunctionRegistryUtil;
import com.sap.sse.datamining.test.util.FunctionTestsUtil;
import com.sap.sse.datamining.test.util.TestsUtil;
import com.sap.sse.datamining.test.util.components.TestLegOfCompetitorWithContextRetrievalProcessor;
import com.sap.sse.datamining.test.util.components.TestRaceWithContextRetrievalProcessor;
import com.sap.sse.datamining.test.util.components.TestRegattaRetrievalProcessor;
import com.sap.sse.i18n.ResourceBundleStringMessages;

public class TestFunctionManagerAsFunctionProvider {
    
    private static final ResourceBundleStringMessages stringMessages = TestsUtil.getTestStringMessagesWithProductiveMessages();
    
    private ExpectedFunctionRegistryUtil functionRegistryUtil;
    private ModifiableDataMiningServer server;
    
    @Before
    public void initializeDataMiningServer() throws NoSuchMethodException, SecurityException {
        functionRegistryUtil = new ExpectedFunctionRegistryUtil();
        server = TestsUtil.createNewServer();
        server.addStringMessages(TestsUtil.getTestStringMessages());
        
        Collection<Class<?>> internalClassesToScan = new HashSet<>();
        internalClassesToScan.add(Test_HasLegOfCompetitorContext.class);
        internalClassesToScan.add(Test_HasRaceContext.class);
        server.registerAllClasses(internalClassesToScan);
        
        Collection<Class<?>> externalClassesToScan = new HashSet<>();
        externalClassesToScan.add(Test_ExternalLibraryClass.class);
        server.registerAllWithExternalFunctionPolicy(externalClassesToScan);
    }
    
    @Test
    public void testGetAllStatistics() {
        Collection<Function<?>> expectedFunctions = functionRegistryUtil.getAllExpectedStatistics();
        assertThat(server.getAllStatistics(), is(expectedFunctions));
    }

    @Test
    public void testGetDimensionsForType() {
        
        Collection<Function<?>> expectedDimensions = functionRegistryUtil.getExpectedDimensionsFor(Test_HasRaceContext.class);
        assertThat(server.getDimensionsFor(Test_HasRaceContext.class), is(expectedDimensions));
        assertThat(server.getDimensionsFor(Test_HasRaceContextImpl.class), is(expectedDimensions));

        expectedDimensions.addAll(functionRegistryUtil.getExpectedDimensionsFor(Test_HasLegOfCompetitorContext.class));
        assertThat(server.getDimensionsFor(Test_HasLegOfCompetitorContext.class), is(expectedDimensions));
        assertThat(server.getDimensionsFor(Test_HasLegOfCompetitorContextImpl.class), is(expectedDimensions));
    }
    
    @Test
    public void testGetDimensionsForDataRetrieverChainDefinition() {
        DataRetrieverChainDefinition<Collection<Test_Regatta>, Test_HasLegOfCompetitorContext> dataRetrieverChainDefinition = createDataRetrieverChainDefinition();

        Collection<Function<?>> expectedDimensions = functionRegistryUtil.getExpectedDimensionsFor(Test_HasRaceContext.class);
        expectedDimensions.addAll(functionRegistryUtil.getExpectedDimensionsFor(Test_HasLegOfCompetitorContext.class));
        assertThat(server.getDimensionsFor(dataRetrieverChainDefinition), is(expectedDimensions));
    }
    
    @SuppressWarnings("unchecked")
    public DataRetrieverChainDefinition<Collection<Test_Regatta>, Test_HasLegOfCompetitorContext> createDataRetrieverChainDefinition() {
        DataRetrieverChainDefinition<Collection<Test_Regatta>, Test_HasLegOfCompetitorContext> dataRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>((Class<Collection<Test_Regatta>>)(Class<?>) Collection.class, Test_HasLegOfCompetitorContext.class, "TestRetrieverChain");
        Class<Processor<Collection<Test_Regatta>, Test_Regatta>> regattaRetrieverClass = (Class<Processor<Collection<Test_Regatta>, Test_Regatta>>)(Class<?>) TestRegattaRetrievalProcessor.class;
        dataRetrieverChainDefinition.startWith(regattaRetrieverClass, Test_Regatta.class, "regatta");
        
        Class<Processor<Test_Regatta, Test_HasRaceContext>> raceRetrieverClass = 
                (Class<Processor<Test_Regatta, Test_HasRaceContext>>)(Class<?>) TestRaceWithContextRetrievalProcessor.class;
        dataRetrieverChainDefinition.addAfter(regattaRetrieverClass,
                                               raceRetrieverClass,
                                               Test_HasRaceContext.class, "race");
        
        Class<Processor<Test_HasRaceContext, Test_HasLegOfCompetitorContext>> legRetrieverClass = 
                (Class<Processor<Test_HasRaceContext, Test_HasLegOfCompetitorContext>>)(Class<?>) TestLegOfCompetitorWithContextRetrievalProcessor.class;
        dataRetrieverChainDefinition.addAfter(raceRetrieverClass,
                                               legRetrieverClass,
                                               Test_HasLegOfCompetitorContext.class, "legOfCompetitor");
        
        return dataRetrieverChainDefinition;
    }
    
    @Test
    public void testGetStatisticsForType() {
        Collection<Function<?>> expectedStatistics = functionRegistryUtil.getExpectedStatisticsFor(Test_HasLegOfCompetitorContext.class);
        assertThat(server.getStatisticsFor(Test_HasLegOfCompetitorContext.class), is(expectedStatistics));
        assertThat(server.getStatisticsFor(Test_HasLegOfCompetitorContextImpl.class), is(expectedStatistics));
    }
    
    @Test
    public void testGetAllFunctionsForType() {
        Collection<Function<?>> expectedFunctions = functionRegistryUtil.getExpectedDimensionsFor(Test_HasRaceContext.class);
        expectedFunctions.addAll(functionRegistryUtil.getExpectedDimensionsFor(Test_HasLegOfCompetitorContext.class));
        expectedFunctions.addAll(functionRegistryUtil.getExpectedStatisticsFor(Test_HasLegOfCompetitorContext.class));
        assertThat(server.getFunctionsFor(Test_HasLegOfCompetitorContext.class), is(expectedFunctions));
        assertThat(server.getFunctionsFor(Test_HasLegOfCompetitorContextImpl.class), is(expectedFunctions));
    }
    
    @Test
    public void testGetFunctionForDTO() throws NoSuchMethodException, SecurityException {
        Method getRegattaMethod = Test_HasRaceContext.class.getMethod("getRace", new Class<?>[0]);
        Function<?> getRegatta = FunctionTestsUtil.getFunctionFactory().createMethodWrappingFunction(getRegattaMethod);
        Method getNameMethod = Test_Named.class.getMethod("getName", new Class<?>[0]);
        Function<?> getName = FunctionTestsUtil.getFunctionFactory().createMethodWrappingFunction(getNameMethod);
        Function<Object> getRegattaName = FunctionTestsUtil.getFunctionFactory().createCompoundFunction(Arrays.asList(getRegatta, getName));
        
        FunctionDTO getRegattaNameDTO = FunctionTestsUtil.getFunctionDTOFactory().createFunctionDTO(getRegattaName, stringMessages, Locale.ENGLISH);
        
        @SuppressWarnings("unchecked") // Hamcrest requires type matching of actual and expected type, so the Functions have to be specific (without <?>)
        Function<Object> providedFunction = (Function<Object>) server.getFunctionForDTO(getRegattaNameDTO);
        assertThat(providedFunction, is(getRegattaName));
    }
    
    @Test
    public void testGetFunctionForUnregisteredDTO() {
        Method illegalDimensionMethod = FunctionTestsUtil.getMethodFromClass(SimpleClassWithMarkedMethods.class, "illegalDimension");
        Function<Object> illegalDimension = FunctionTestsUtil.getFunctionFactory().createMethodWrappingFunction(illegalDimensionMethod);
        FunctionDTO illegalDimensionDTO = FunctionTestsUtil.getFunctionDTOFactory().createFunctionDTO(illegalDimension, stringMessages, Locale.ENGLISH);
        
        assertThat(server.getFunctionForDTO(illegalDimensionDTO), is(nullValue()));
    }
    
    @Test
    public void testGetFunctionForNullDTO() {
        assertThat(server.getFunctionForDTO(null), is(nullValue()));
    }

}
