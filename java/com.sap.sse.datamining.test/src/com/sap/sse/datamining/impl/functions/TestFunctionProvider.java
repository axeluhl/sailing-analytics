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

import com.sap.sse.datamining.factories.FunctionDTOFactory;
import com.sap.sse.datamining.factories.FunctionFactory;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.functions.FunctionProvider;
import com.sap.sse.datamining.functions.FunctionRegistry;
import com.sap.sse.datamining.i18n.DataMiningStringMessages;
import com.sap.sse.datamining.shared.dto.FunctionDTO;
import com.sap.sse.datamining.test.functions.registry.test_classes.Test_Named;
import com.sap.sse.datamining.test.functions.registry.test_contexts.Test_HasLegContext;
import com.sap.sse.datamining.test.functions.registry.test_contexts.Test_HasLegContextImpl;
import com.sap.sse.datamining.test.functions.registry.test_contexts.Test_HasRaceContext;
import com.sap.sse.datamining.test.functions.registry.test_contexts.Test_HasRaceContextImpl;
import com.sap.sse.datamining.test.functions.test_classes.SimpleClassWithMarkedMethods;
import com.sap.sse.datamining.test.functions.test_classes.Test_ExternalLibraryClass;
import com.sap.sse.datamining.test.util.ExpectedFunctionRegistryUtil;
import com.sap.sse.datamining.test.util.FunctionTestsUtil;
import com.sap.sse.datamining.test.util.TestsUtil;

public class TestFunctionProvider {
    
    private static final DataMiningStringMessages stringMessages = TestsUtil.getTestStringMessagesWithProductiveMessages();
    
    private ExpectedFunctionRegistryUtil functionRegistryUtil;
    private FunctionRegistry functionRegistry;
    
    @Before
    public void initializeFunctionRegistry() throws NoSuchMethodException, SecurityException {
        functionRegistryUtil = new ExpectedFunctionRegistryUtil();
        functionRegistry = new SimpleFunctionRegistry();
        
        Collection<Class<?>> internalClassesToScan = new HashSet<>();
        internalClassesToScan.add(Test_HasLegContext.class);
        internalClassesToScan.add(Test_HasRaceContext.class);
        functionRegistry.registerAllWithInternalFunctionPolicy(internalClassesToScan);
        
        Collection<Class<?>> externalClassesToScan = new HashSet<>();
        externalClassesToScan.add(Test_ExternalLibraryClass.class);
        functionRegistry.registerAllWithExternalFunctionPolicy(externalClassesToScan);
    }

    @Test
    public void testGetDimensionsForType() {
        FunctionProvider functionProvider = new RegistryFunctionProvider(functionRegistry);
        
        Collection<Function<?>> expectedDimensions = functionRegistryUtil.getExpectedDimensionsFor(Test_HasRaceContext.class);
        assertThat(functionProvider.getDimensionsFor(Test_HasRaceContext.class), is(expectedDimensions));
        assertThat(functionProvider.getDimensionsFor(Test_HasRaceContextImpl.class), is(expectedDimensions));

        expectedDimensions.addAll(functionRegistryUtil.getExpectedDimensionsFor(Test_HasLegContext.class));
        assertThat(functionProvider.getDimensionsFor(Test_HasLegContext.class), is(expectedDimensions));
        assertThat(functionProvider.getDimensionsFor(Test_HasLegContextImpl.class), is(expectedDimensions));
    }
    
    @Test
    public void testGetStatisticsForType() {
        FunctionProvider functionProvider = new RegistryFunctionProvider(functionRegistry);
        Collection<Function<?>> expectedFunctions = functionRegistryUtil.getExpectedStatisticsFor(Test_HasLegContext.class);
        assertThat(functionProvider.getStatisticsFor(Test_HasLegContext.class), is(expectedFunctions));
        assertThat(functionProvider.getStatisticsFor(Test_HasLegContextImpl.class), is(expectedFunctions));
    }
    
    @Test
    public void testGetFunctionForDTO() throws NoSuchMethodException, SecurityException {
        Method getRegattaMethod = Test_HasRaceContext.class.getMethod("getRegatta", new Class<?>[0]);
        Function<?> getRegatta = FunctionFactory.createMethodWrappingFunction(getRegattaMethod);
        Method getNameMethod = Test_Named.class.getMethod("getName", new Class<?>[0]);
        Function<?> getName = FunctionFactory.createMethodWrappingFunction(getNameMethod);
        Function<Object> getRegattaName = FunctionFactory.createCompoundFunction(Arrays.asList(getRegatta, getName));
        
        FunctionDTO getRegattaNameDTO = FunctionDTOFactory.createFunctionDTO(getRegattaName, Locale.ENGLISH, stringMessages);
        
        FunctionProvider functionProvider = new RegistryFunctionProvider(functionRegistry);
        @SuppressWarnings("unchecked") // Hamcrest requires type matching of actual and expected type, so the Functions have to be specific (without <?>)
        Function<Object> providedFunction = (Function<Object>) functionProvider.getFunctionForDTO(getRegattaNameDTO);
        assertThat(providedFunction, is(getRegattaName));
    }
    
    @Test
    public void testGetFunctionForUnregisteredDTO() {
        Method incrementMethod = FunctionTestsUtil.getMethodFromClass(SimpleClassWithMarkedMethods.class, "increment", int.class);
        Function<Object> increment = FunctionFactory.createMethodWrappingFunction(incrementMethod);
        FunctionDTO incrementDTO = FunctionDTOFactory.createFunctionDTO(increment, Locale.ENGLISH, stringMessages);
        
        FunctionProvider functionProvider = new RegistryFunctionProvider(functionRegistry);
        assertThat(functionProvider.getFunctionForDTO(incrementDTO), is(nullValue()));
    }
    
    @Test
    public void testGetFunctionForNullDTO() {
        FunctionProvider functionProvider = new RegistryFunctionProvider(functionRegistry);
        assertThat(functionProvider.getFunctionForDTO(null), is(nullValue()));
    }

}
