package com.sap.sse.datamining.impl.functions;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;
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
import com.sap.sse.datamining.test.functions.registry.test_contexts.Test_HasLegContext;
import com.sap.sse.datamining.test.functions.test_classes.ContainerElement;
import com.sap.sse.datamining.test.functions.test_classes.DataTypeWithContext;
import com.sap.sse.datamining.test.functions.test_classes.DataTypeWithContextImpl;
import com.sap.sse.datamining.test.functions.test_classes.Test_ExternalLibraryClass;
import com.sap.sse.datamining.test.functions.test_classes.MarkedContainer;
import com.sap.sse.datamining.test.functions.test_classes.SimpleClassWithMarkedMethods;
import com.sap.sse.datamining.test.util.FunctionTestsUtil;
import com.sap.sse.datamining.test.util.TestsUtil;

public class TestFunctionProvider {
    
    private static final DataMiningStringMessages stringMessages = TestsUtil.getTestStringMessagesWithProductiveMessages();
    
    private FunctionRegistry functionRegistry;
    
    @Before
    public void initializeFunctionRegistry() {
        functionRegistry = new SimpleFunctionRegistry();
        
        Collection<Class<?>> internalClassesToScan = new HashSet<>();
        internalClassesToScan.add(Test_HasLegContext.class);
        functionRegistry.registerAllWithInternalFunctionPolicy(internalClassesToScan);
        
        Collection<Class<?>> externalClassesToScan = new HashSet<>();
        externalClassesToScan.add(Test_ExternalLibraryClass.class);
        functionRegistry.registerAllWithExternalFunctionPolicy(externalClassesToScan);
    }

    @Test
    public void testGetDimensionsForType() {
        FunctionProvider functionProvider = new RegistryFunctionProvider(functionRegistry);
        
        Collection<Function<?>> expectedDimensions = FunctionTestsUtil.getDimensionsFor(DataTypeWithContext.class);
        Collection<Function<?>> providedDimensions = new HashSet<>(functionProvider.getDimensionsFor(DataTypeWithContext.class));
        assertThat(providedDimensions, is(expectedDimensions));
        
        providedDimensions = new HashSet<>(functionProvider.getDimensionsFor(DataTypeWithContextImpl.class));
        assertThat(providedDimensions, is(expectedDimensions));
    }
    
    @Test
    public void testGetFunctionsForType() {
        FunctionProvider functionProvider = new RegistryFunctionProvider(functionRegistry);
        
        Collection<Function<?>> expectedFunctions = FunctionTestsUtil.getMarkedMethodsOfDataTypeWithContextAndItsSupertypes();
        Collection<Function<?>> providedFunctions = new HashSet<>(functionProvider.getFunctionsFor(DataTypeWithContext.class));
        assertThat(providedFunctions, is(expectedFunctions));
        
        expectedFunctions = FunctionTestsUtil.getMarkedMethodsOfDataTypeWithContextImplAndItsSupertypes();
        providedFunctions = new HashSet<>(functionProvider.getFunctionsFor(DataTypeWithContextImpl.class));
        assertThat(providedFunctions, is(expectedFunctions));
    }
    
    @Test
    public void testGetFunctionForDTO() {
        Method getRegattaNameMethod = FunctionTestsUtil.getMethodFromClass(DataTypeWithContext.class, "getRegattaName");
        Function<Object> getRegattaName = FunctionFactory.createMethodWrappingFunction(getRegattaNameMethod);
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
    
    @Test
    public void testGetTransitiveDimension() throws ClassCastException, NoSuchMethodException, SecurityException {
        FunctionProvider functionProvider = new RegistryFunctionProvider(functionRegistry);
        
        Collection<Function<?>> expectedDimensions = new HashSet<>();
        expectedDimensions.add(FunctionFactory.createMethodWrappingFunction(ContainerElement.class.getMethod("getName",
                new Class<?>[0])));
        assertThat(functionProvider.getTransitiveDimensionsFor(MarkedContainer.class, 1), is(expectedDimensions));
    }

}
