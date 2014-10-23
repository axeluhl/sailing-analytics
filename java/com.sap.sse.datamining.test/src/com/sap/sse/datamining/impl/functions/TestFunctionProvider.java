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

import com.sap.sse.datamining.DataRetrieverChainDefinition;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.functions.FunctionProvider;
import com.sap.sse.datamining.functions.FunctionRegistry;
import com.sap.sse.datamining.i18n.DataMiningStringMessages;
import com.sap.sse.datamining.impl.SimpleDataRetrieverChainDefinition;
import com.sap.sse.datamining.shared.dto.FunctionDTO;
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

public class TestFunctionProvider {
    
    private static final DataMiningStringMessages stringMessages = TestsUtil.getTestStringMessagesWithProductiveMessages();
    
    private ExpectedFunctionRegistryUtil functionRegistryUtil;
    private FunctionRegistry functionRegistry;
    
    @Before
    public void initializeFunctionRegistry() throws NoSuchMethodException, SecurityException {
        functionRegistryUtil = new ExpectedFunctionRegistryUtil();
        functionRegistry = new SimpleFunctionRegistry();
        
        Collection<Class<?>> internalClassesToScan = new HashSet<>();
        internalClassesToScan.add(Test_HasLegOfCompetitorContext.class);
        internalClassesToScan.add(Test_HasRaceContext.class);
        functionRegistry.registerAllWithInternalFunctionPolicy(internalClassesToScan);
        
        Collection<Class<?>> externalClassesToScan = new HashSet<>();
        externalClassesToScan.add(Test_ExternalLibraryClass.class);
        functionRegistry.registerAllWithExternalFunctionPolicy(externalClassesToScan);
    }
    
    @Test
    public void testGetAllStatistics() {
        FunctionProvider functionProvider = new RegistryFunctionProvider(functionRegistry);
        Collection<Function<?>> expectedFunctions = functionRegistryUtil.getAllExpectedStatistics();
        assertThat(functionProvider.getAllStatistics(), is(expectedFunctions));
    }

    @Test
    public void testGetDimensionsForType() {
        FunctionProvider functionProvider = new RegistryFunctionProvider(functionRegistry);
        
        Collection<Function<?>> expectedDimensions = functionRegistryUtil.getExpectedDimensionsFor(Test_HasRaceContext.class);
        assertThat(functionProvider.getDimensionsFor(Test_HasRaceContext.class), is(expectedDimensions));
        assertThat(functionProvider.getDimensionsFor(Test_HasRaceContextImpl.class), is(expectedDimensions));

        expectedDimensions.addAll(functionRegistryUtil.getExpectedDimensionsFor(Test_HasLegOfCompetitorContext.class));
        assertThat(functionProvider.getDimensionsFor(Test_HasLegOfCompetitorContext.class), is(expectedDimensions));
        assertThat(functionProvider.getDimensionsFor(Test_HasLegOfCompetitorContextImpl.class), is(expectedDimensions));
    }
    
    @Test
    public void testGetDimensionsForDataRetrieverChainDefinition() {
        FunctionProvider functionProvider = new RegistryFunctionProvider(functionRegistry);
        DataRetrieverChainDefinition<Collection<Test_Regatta>> dataRetrieverChainDefinition = createDataRetrieverChainDefinition();

        Collection<Function<?>> expectedDimensions = functionRegistryUtil.getExpectedDimensionsFor(Test_HasRaceContext.class);
        expectedDimensions.addAll(functionRegistryUtil.getExpectedDimensionsFor(Test_HasLegOfCompetitorContext.class));
        assertThat(functionProvider.getMinimizedDimensionsFor(dataRetrieverChainDefinition), is(expectedDimensions));
    }
    
    @SuppressWarnings("unchecked")
    public DataRetrieverChainDefinition<Collection<Test_Regatta>> createDataRetrieverChainDefinition() {
        DataRetrieverChainDefinition<Collection<Test_Regatta>> dataRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>((Class<Collection<Test_Regatta>>)(Class<?>) Collection.class, "TestRetrieverChain");
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
        
        return dataRetrieverChainDefinition;
    }
    
    @Test
    public void testGetStatisticsForType() {
        FunctionProvider functionProvider = new RegistryFunctionProvider(functionRegistry);
        Collection<Function<?>> expectedStatistics = functionRegistryUtil.getExpectedStatisticsFor(Test_HasLegOfCompetitorContext.class);
        assertThat(functionProvider.getStatisticsFor(Test_HasLegOfCompetitorContext.class), is(expectedStatistics));
        assertThat(functionProvider.getStatisticsFor(Test_HasLegOfCompetitorContextImpl.class), is(expectedStatistics));
    }
    
    @Test
    public void testGetAllFunctionsForType() {
        FunctionProvider functionProvider = new RegistryFunctionProvider(functionRegistry);
        
        Collection<Function<?>> expectedFunctions = functionRegistryUtil.getExpectedDimensionsFor(Test_HasRaceContext.class);
        expectedFunctions.addAll(functionRegistryUtil.getExpectedDimensionsFor(Test_HasLegOfCompetitorContext.class));
        expectedFunctions.addAll(functionRegistryUtil.getExpectedStatisticsFor(Test_HasLegOfCompetitorContext.class));
        assertThat(functionProvider.getFunctionsFor(Test_HasLegOfCompetitorContext.class), is(expectedFunctions));
        assertThat(functionProvider.getFunctionsFor(Test_HasLegOfCompetitorContextImpl.class), is(expectedFunctions));
    }
    
    @Test
    public void testGetFunctionForDTO() throws NoSuchMethodException, SecurityException {
        Method getRegattaMethod = Test_HasRaceContext.class.getMethod("getRace", new Class<?>[0]);
        Function<?> getRegatta = FunctionTestsUtil.getFunctionFactory().createMethodWrappingFunction(getRegattaMethod);
        Method getNameMethod = Test_Named.class.getMethod("getName", new Class<?>[0]);
        Function<?> getName = FunctionTestsUtil.getFunctionFactory().createMethodWrappingFunction(getNameMethod);
        Function<Object> getRegattaName = FunctionTestsUtil.getFunctionFactory().createCompoundFunction(Arrays.asList(getRegatta, getName));
        
        FunctionDTO getRegattaNameDTO = FunctionTestsUtil.getFunctionDTOFactory().createFunctionDTO(getRegattaName, stringMessages, Locale.ENGLISH);
        
        FunctionProvider functionProvider = new RegistryFunctionProvider(functionRegistry);
        @SuppressWarnings("unchecked") // Hamcrest requires type matching of actual and expected type, so the Functions have to be specific (without <?>)
        Function<Object> providedFunction = (Function<Object>) functionProvider.getFunctionForDTO(getRegattaNameDTO);
        assertThat(providedFunction, is(getRegattaName));
    }
    
    @Test
    public void testGetFunctionForUnregisteredDTO() {
        Method incrementMethod = FunctionTestsUtil.getMethodFromClass(SimpleClassWithMarkedMethods.class, "increment", int.class);
        Function<Object> increment = FunctionTestsUtil.getFunctionFactory().createMethodWrappingFunction(incrementMethod);
        FunctionDTO incrementDTO = FunctionTestsUtil.getFunctionDTOFactory().createFunctionDTO(increment, stringMessages, Locale.ENGLISH);
        
        FunctionProvider functionProvider = new RegistryFunctionProvider(functionRegistry);
        assertThat(functionProvider.getFunctionForDTO(incrementDTO), is(nullValue()));
    }
    
    @Test
    public void testGetFunctionForNullDTO() {
        FunctionProvider functionProvider = new RegistryFunctionProvider(functionRegistry);
        assertThat(functionProvider.getFunctionForDTO(null), is(nullValue()));
    }

}
