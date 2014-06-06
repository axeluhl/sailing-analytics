package com.sap.sse.datamining.impl.functions;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.datamining.factories.FunctionDTOFactory;
import com.sap.sse.datamining.factories.FunctionFactory;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.i18n.DataMiningStringMessages;
import com.sap.sse.datamining.shared.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTOImpl;
import com.sap.sse.datamining.test.functions.test_classes.Test_ExternalLibraryClass;
import com.sap.sse.datamining.test.functions.test_classes.SimpleClassWithMarkedMethods;
import com.sap.sse.datamining.test.util.FunctionTestsUtil;
import com.sap.sse.datamining.test.util.TestsUtil;

public class TestFunctionDTOConstruction {
    
    private static final DataMiningStringMessages stringMessages = TestsUtil.getTestStringMessages();
    
    private Method dimensionMethod;
    private Method sideEffectFreeValueMethod;
    private Method externalLibraryMethod;
    private Method incrementMethod;

    @Before
    public void initializeMethods() {
        dimensionMethod = FunctionTestsUtil.getMethodFromClass(SimpleClassWithMarkedMethods.class, "dimension");
        sideEffectFreeValueMethod = FunctionTestsUtil.getMethodFromClass(SimpleClassWithMarkedMethods.class, "sideEffectFreeValue");
        externalLibraryMethod = FunctionTestsUtil.getMethodFromClass(Test_ExternalLibraryClass.class, "foo");
        incrementMethod = FunctionTestsUtil.getMethodFromClass(SimpleClassWithMarkedMethods.class, "increment", int.class);
    }

    @Test
    public void testDimensionDTOConstruction() {
        Function<?> dimension = FunctionFactory.createMethodWrappingFunction(dimensionMethod);
        
        FunctionDTO expectedDimensionDTO = createExpectedDimensionDTO();
        assertThat(FunctionDTOFactory.createFunctionDTO(dimension), is(expectedDimensionDTO));
        assertThat(FunctionDTOFactory.createFunctionDTO(dimension, Locale.ENGLISH, stringMessages), is(expectedDimensionDTO));
        assertThat(FunctionDTOFactory.createFunctionDTO(dimension, Locale.GERMAN, stringMessages), is(expectedDimensionDTO));
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
        assertThat(FunctionDTOFactory.createFunctionDTO(sideEffectFreeValue, Locale.ENGLISH, stringMessages), is(expectedDTO));
        assertThat(FunctionDTOFactory.createFunctionDTO(sideEffectFreeValue, Locale.GERMAN, stringMessages), is(expectedDTO));
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
        assertThat(FunctionDTOFactory.createFunctionDTO(externalLibraryFunction, Locale.ENGLISH, stringMessages), is(expectedDTO));
        assertThat(FunctionDTOFactory.createFunctionDTO(externalLibraryFunction, Locale.GERMAN, stringMessages), is(expectedDTO));
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
        assertThat(FunctionDTOFactory.createFunctionDTO(increment, Locale.ENGLISH, stringMessages), is(expectedDTO));
        assertThat(FunctionDTOFactory.createFunctionDTO(increment, Locale.GERMAN, stringMessages), is(expectedDTO));
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
