package com.sap.sse.datamining.impl.functions;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;
import java.util.ArrayList;
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
import com.sap.sse.datamining.functions.ParameterProvider;
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

public class TestFunctionManagerAsFunctionProvider {
    
    private static final DataMiningStringMessages stringMessages = TestsUtil.getTestStringMessagesWithProductiveMessages();
    
    private ExpectedFunctionRegistryUtil functionRegistryUtil;
    private FunctionManager functionManager;
    
    @Before
    public void initializeFunctionRegistry() throws NoSuchMethodException, SecurityException {
        functionRegistryUtil = new ExpectedFunctionRegistryUtil();
        functionManager = new FunctionManager();
        
        Collection<Class<?>> internalClassesToScan = new HashSet<>();
        internalClassesToScan.add(Test_HasLegOfCompetitorContext.class);
        internalClassesToScan.add(Test_HasRaceContext.class);
        functionManager.registerAllWithInternalFunctionPolicy(internalClassesToScan);
        
        Collection<Class<?>> externalClassesToScan = new HashSet<>();
        externalClassesToScan.add(Test_ExternalLibraryClass.class);
        functionManager.registerAllWithExternalFunctionPolicy(externalClassesToScan);
    }
    
    @Test
    public void testGetAllStatistics() {
        Collection<Function<?>> expectedFunctions = functionRegistryUtil.getAllExpectedStatistics();
        assertThat(functionManager.getAllStatistics(), is(expectedFunctions));
    }

    @Test
    public void testGetDimensionsForType() {
        
        Collection<Function<?>> expectedDimensions = functionRegistryUtil.getExpectedDimensionsFor(Test_HasRaceContext.class);
        assertThat(functionManager.getDimensionsFor(Test_HasRaceContext.class), is(expectedDimensions));
        assertThat(functionManager.getDimensionsFor(Test_HasRaceContextImpl.class), is(expectedDimensions));

        expectedDimensions.addAll(functionRegistryUtil.getExpectedDimensionsFor(Test_HasLegOfCompetitorContext.class));
        assertThat(functionManager.getDimensionsFor(Test_HasLegOfCompetitorContext.class), is(expectedDimensions));
        assertThat(functionManager.getDimensionsFor(Test_HasLegOfCompetitorContextImpl.class), is(expectedDimensions));
    }
    
    @Test
    public void testGetDimensionsForDataRetrieverChainDefinition() {
        DataRetrieverChainDefinition<Collection<Test_Regatta>, Test_HasLegOfCompetitorContext> dataRetrieverChainDefinition = createDataRetrieverChainDefinition();

        Collection<Function<?>> expectedDimensions = functionRegistryUtil.getExpectedDimensionsFor(Test_HasRaceContext.class);
        expectedDimensions.addAll(functionRegistryUtil.getExpectedDimensionsFor(Test_HasLegOfCompetitorContext.class));
        assertThat(functionManager.getDimensionsFor(dataRetrieverChainDefinition), is(expectedDimensions));
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
        assertThat(functionManager.getStatisticsFor(Test_HasLegOfCompetitorContext.class), is(expectedStatistics));
        assertThat(functionManager.getStatisticsFor(Test_HasLegOfCompetitorContextImpl.class), is(expectedStatistics));
    }
    
    @Test
    public void testGetAllFunctionsForType() {
        Collection<Function<?>> expectedFunctions = functionRegistryUtil.getExpectedDimensionsFor(Test_HasRaceContext.class);
        expectedFunctions.addAll(functionRegistryUtil.getExpectedDimensionsFor(Test_HasLegOfCompetitorContext.class));
        expectedFunctions.addAll(functionRegistryUtil.getExpectedStatisticsFor(Test_HasLegOfCompetitorContext.class));
        assertThat(functionManager.getFunctionsFor(Test_HasLegOfCompetitorContext.class), is(expectedFunctions));
        assertThat(functionManager.getFunctionsFor(Test_HasLegOfCompetitorContextImpl.class), is(expectedFunctions));
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
        Function<Object> providedFunction = (Function<Object>) functionManager.getFunctionForDTO(getRegattaNameDTO);
        assertThat(providedFunction, is(getRegattaName));
    }
    
    @Test
    public void testGetFunctionForUnregisteredDTO() {
        Method illegalDimensionMethod = FunctionTestsUtil.getMethodFromClass(SimpleClassWithMarkedMethods.class, "illegalDimension");
        Function<Object> illegalDimension = FunctionTestsUtil.getFunctionFactory().createMethodWrappingFunction(illegalDimensionMethod);
        FunctionDTO illegalDimensionDTO = FunctionTestsUtil.getFunctionDTOFactory().createFunctionDTO(illegalDimension, stringMessages, Locale.ENGLISH);
        
        assertThat(functionManager.getFunctionForDTO(illegalDimensionDTO), is(nullValue()));
    }
    
    @Test
    public void testGetFunctionForNullDTO() {
        assertThat(functionManager.getFunctionForDTO(null), is(nullValue()));
    }
    
    @Test
    public void testGetParameterProviderForNullaryFunction() {
        Method nullaryDimensionMethod = FunctionTestsUtil.getMethodFromClass(SimpleClassWithMarkedMethods.class, "dimension");
        Function<Object> nullaryDimension = FunctionTestsUtil.getFunctionFactory().createMethodWrappingFunction(nullaryDimensionMethod);
        
        FunctionProvider functionProvider = new FunctionManager();
        assertThat(functionProvider.getParameterProviderFor(nullaryDimension), is(ParameterProvider.NULL));
    }
    
    @Test
    public void testGetPrameterProviderForFunctionsWithParameters() {
        FunctionManager functionManager = new FunctionManager();
        ParameterProvider intParameterProvider = new ParameterProvider() {
            @Override
            public Object[] getParameters() {
                return new Object[] {1};
            }
            
            @Override
            public Iterable<Class<?>> getParameterTypes() {
                Collection<Class<?>> parameterTypes = new ArrayList<>();
                parameterTypes.add(int.class);
                return parameterTypes;
            }
        };
        functionManager.registerParameterProvider(intParameterProvider);
        LocalizationParameterProvider stringMessagesParameterProvider = new LocalizationParameterProvider(Locale.ENGLISH, stringMessages);
        functionManager.registerParameterProvider(stringMessagesParameterProvider);
        
        Method incrementMethod = FunctionTestsUtil.getMethodFromClass(SimpleClassWithMarkedMethods.class, "increment", int.class);
        Function<Object> increment = FunctionTestsUtil.getFunctionFactory().createMethodWrappingFunction(incrementMethod);
        assertThat(functionManager.getParameterProviderFor(increment), is(intParameterProvider));
        
        Method getLocalizedNameMethod = FunctionTestsUtil.getMethodFromClass(SimpleClassWithMarkedMethods.class, "getLocalizedName", Locale.class, DataMiningStringMessages.class);
        Function<Object> getLocalizedName = FunctionTestsUtil.getFunctionFactory().createMethodWrappingFunction(getLocalizedNameMethod);
        assertThat(functionManager.getParameterProviderFor(getLocalizedName), is(stringMessagesParameterProvider));
    }
    
    @Test
    public void testGetParameterProviderForFunctionWithoutAnAvailableParameterProvider() {
        FunctionManager functionManager = new FunctionManager();
        //Adding a wrong ParameterProvider to increase code coverage
        functionManager.registerParameterProvider(new LocalizationParameterProvider(Locale.ENGLISH, stringMessages));
        
        Method incrementMethod = FunctionTestsUtil.getMethodFromClass(SimpleClassWithMarkedMethods.class, "increment", int.class);
        Function<Object> increment = FunctionTestsUtil.getFunctionFactory().createMethodWrappingFunction(incrementMethod);
        assertThat(functionManager.getParameterProviderFor(increment), nullValue());
    }

}
