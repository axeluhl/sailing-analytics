package com.sap.sailing.datamining.function.impl;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.datamining.factories.FunctionFactory;
import com.sap.sailing.datamining.test.function.test_classes.ExternalLibraryClass;
import com.sap.sailing.datamining.test.function.test_classes.SimpleClassWithMarkedMethods;
import com.sap.sailing.datamining.test.util.FunctionTestsUtil;
import com.sap.sailing.datamining.test.util.StringMessagesForTests;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.shared.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTOImpl;

public class TestFunctionDTOConstruction {
    
    private static final StringMessagesForTests stringMessages = new StringMessagesForTests();
    
    private Method dimensionMethod;
    private Method sideEffectFreeValueMethod;
    private Method externalLibraryMethod;
    private Method incrementMethod;

    @Before
    public void initializeMethods() {
        dimensionMethod = FunctionTestsUtil.getMethodFromClass(SimpleClassWithMarkedMethods.class, "dimension");
        sideEffectFreeValueMethod = FunctionTestsUtil.getMethodFromClass(SimpleClassWithMarkedMethods.class, "sideEffectFreeValue");
        externalLibraryMethod = FunctionTestsUtil.getMethodFromClass(ExternalLibraryClass.class, "foo");
        incrementMethod = FunctionTestsUtil.getMethodFromClass(SimpleClassWithMarkedMethods.class, "increment", int.class);
    }

    @Test
    public void testDimensionDTOConstruction() {
        Function<?> dimension = FunctionFactory.createMethodWrappingFunction(dimensionMethod);
        
        FunctionDTO expectedDimensionDTO = createExpectedDimensionDTO();
        assertThat(dimension.asDTO(), is(expectedDimensionDTO));
        assertThat(dimension.asDTO(Locale.ENGLISH, stringMessages), is(expectedDimensionDTO));
        assertThat(dimension.asDTO(Locale.GERMAN, stringMessages), is(expectedDimensionDTO));
    }

    public FunctionDTOImpl createExpectedDimensionDTO() {
        String functionName = dimensionMethod.getName();
        String sourceTypeName = dimensionMethod.getDeclaringClass().getSimpleName();
        String returnTypeName = dimensionMethod.getReturnType().getSimpleName();
        List<String> parameterTypeNames = new ArrayList<>();

        String displayName = functionName;
        boolean isDimension = true;
        
        return new FunctionDTOImpl(functionName, sourceTypeName, returnTypeName, parameterTypeNames, displayName, isDimension);
    }
    
    @Test
    public void testNullarySideEffectFreeValueDTOConstruction() {
        Function<?> sideEffectFreeValue = FunctionFactory.createMethodWrappingFunction(sideEffectFreeValueMethod);
        
        FunctionDTO expectedDTO = createExpectedNullarySideEffectFreeValueDTO();
        assertThat(sideEffectFreeValue.asDTO(Locale.ENGLISH, stringMessages), is(expectedDTO));
        assertThat(sideEffectFreeValue.asDTO(Locale.GERMAN, stringMessages), is(expectedDTO));
    }

    private FunctionDTO createExpectedNullarySideEffectFreeValueDTO() {
        String functionName = sideEffectFreeValueMethod.getName();
        String sourceTypeName = sideEffectFreeValueMethod.getDeclaringClass().getSimpleName();
        String returnTypeName = sideEffectFreeValueMethod.getReturnType().getSimpleName();
        List<String> parameterTypeNames = new ArrayList<>();

        String displayName = functionName;
        boolean isDimension = false;
        
        return new FunctionDTOImpl(functionName, sourceTypeName, returnTypeName, parameterTypeNames, displayName, isDimension);
    }
    
    @Test
    public void testExternalLibraryFunctionDTOConstruction() {
        Function<?> externalLibraryFunction = FunctionFactory.createMethodWrappingFunction(externalLibraryMethod);
        
        FunctionDTO expectedDTO = createExpectedExternalLibraryFunctionDTO();
        assertThat(externalLibraryFunction.asDTO(Locale.ENGLISH, stringMessages), is(expectedDTO));
        assertThat(externalLibraryFunction.asDTO(Locale.GERMAN, stringMessages), is(expectedDTO));
    }

    private FunctionDTO createExpectedExternalLibraryFunctionDTO() {
        String functionName = externalLibraryMethod.getName();
        String sourceTypeName = externalLibraryMethod.getDeclaringClass().getSimpleName();
        String returnTypeName = externalLibraryMethod.getReturnType().getSimpleName();
        List<String> parameterTypeNames = new ArrayList<>();

        String displayName = functionName;
        boolean isDimension = false;
        
        return new FunctionDTOImpl(functionName, sourceTypeName, returnTypeName, parameterTypeNames, displayName, isDimension);
    }
    
    @Test
    public void testFunctionDTOConstructionForMethodWithParameters() {
        Function<?> increment = FunctionFactory.createMethodWrappingFunction(incrementMethod);
        
        FunctionDTO expectedDTO = createFunctionDTOWithParameters();
        assertThat(increment.asDTO(Locale.ENGLISH, stringMessages), is(expectedDTO));
        assertThat(increment.asDTO(Locale.GERMAN, stringMessages), is(expectedDTO));
    }

    private FunctionDTO createFunctionDTOWithParameters() {
        String functionName = incrementMethod.getName();
        String sourceTypeName = incrementMethod.getDeclaringClass().getSimpleName();
        String returnTypeName = incrementMethod.getReturnType().getSimpleName();
        List<String> parameterTypeNames = new ArrayList<>();
        parameterTypeNames.add(int.class.getSimpleName());

        String displayName = functionName;
        boolean isDimension = false;
        
        return new FunctionDTOImpl(functionName, sourceTypeName, returnTypeName, parameterTypeNames, displayName, isDimension);
    }

}
