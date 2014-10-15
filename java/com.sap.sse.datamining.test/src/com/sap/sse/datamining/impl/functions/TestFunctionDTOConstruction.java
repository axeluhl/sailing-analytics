package com.sap.sse.datamining.impl.functions;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.i18n.DataMiningStringMessages;
import com.sap.sse.datamining.shared.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTOImpl;
import com.sap.sse.datamining.test.functions.test_classes.SimpleClassWithMarkedMethods;
import com.sap.sse.datamining.test.functions.test_classes.Test_ExternalLibraryClass;
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
        Function<?> dimension = FunctionTestsUtil.getFunctionFactory().createMethodWrappingFunction(dimensionMethod);
        
        FunctionDTO expectedDimensionDTO = createExpectedDimensionDTO();
        assertThat(FunctionTestsUtil.getFunctionDTOFactory().createFunctionDTO(dimension), is(expectedDimensionDTO));
        assertThat(FunctionTestsUtil.getFunctionDTOFactory().createFunctionDTO(dimension, Locale.ENGLISH, stringMessages), is(expectedDimensionDTO));
        assertThat(FunctionTestsUtil.getFunctionDTOFactory().createFunctionDTO(dimension, Locale.GERMAN, stringMessages), is(expectedDimensionDTO));
    }

    public FunctionDTOImpl createExpectedDimensionDTO() {
        String functionName = dimensionMethod.getName();
        String sourceTypeName = dimensionMethod.getDeclaringClass().getSimpleName();
        String returnTypeName = dimensionMethod.getReturnType().getSimpleName();
        List<String> parameterTypeNames = new ArrayList<>();

        String displayName = functionName;
        boolean isDimension = true;
        int ordinal = 0;
        
        return new FunctionDTOImpl(isDimension, functionName, sourceTypeName, returnTypeName, parameterTypeNames, displayName, ordinal);
    }
    
    @Test
    public void testNullarySideEffectFreeValueDTOConstruction() {
        Function<?> sideEffectFreeValue = FunctionTestsUtil.getFunctionFactory().createMethodWrappingFunction(sideEffectFreeValueMethod);
        
        FunctionDTO expectedDTO = createExpectedNullarySideEffectFreeValueDTO();
        assertThat(FunctionTestsUtil.getFunctionDTOFactory().createFunctionDTO(sideEffectFreeValue, Locale.ENGLISH, stringMessages), is(expectedDTO));
        assertThat(FunctionTestsUtil.getFunctionDTOFactory().createFunctionDTO(sideEffectFreeValue, Locale.GERMAN, stringMessages), is(expectedDTO));
    }

    private FunctionDTO createExpectedNullarySideEffectFreeValueDTO() {
        String functionName = sideEffectFreeValueMethod.getName();
        String sourceTypeName = sideEffectFreeValueMethod.getDeclaringClass().getSimpleName();
        String returnTypeName = sideEffectFreeValueMethod.getReturnType().getSimpleName();
        List<String> parameterTypeNames = new ArrayList<>();

        String displayName = functionName;
        boolean isDimension = false;
        int ordinal = 0;
        
        return new FunctionDTOImpl(isDimension, functionName, sourceTypeName, returnTypeName, parameterTypeNames, displayName, ordinal);
    }
    
    @Test
    public void testExternalLibraryFunctionDTOConstruction() {
        Function<?> externalLibraryFunction = FunctionTestsUtil.getFunctionFactory().createMethodWrappingFunction(externalLibraryMethod);
        
        FunctionDTO expectedDTO = createExpectedExternalLibraryFunctionDTO();
        assertThat(FunctionTestsUtil.getFunctionDTOFactory().createFunctionDTO(externalLibraryFunction, Locale.ENGLISH, stringMessages), is(expectedDTO));
        assertThat(FunctionTestsUtil.getFunctionDTOFactory().createFunctionDTO(externalLibraryFunction, Locale.GERMAN, stringMessages), is(expectedDTO));
    }

    private FunctionDTO createExpectedExternalLibraryFunctionDTO() {
        String functionName = externalLibraryMethod.getName();
        String sourceTypeName = externalLibraryMethod.getDeclaringClass().getSimpleName();
        String returnTypeName = externalLibraryMethod.getReturnType().getSimpleName();
        List<String> parameterTypeNames = new ArrayList<>();

        String displayName = functionName;
        boolean isDimension = false;
        int ordinal = 0;
        
        return new FunctionDTOImpl(isDimension, functionName, sourceTypeName, returnTypeName, parameterTypeNames, displayName, ordinal);
    }
    
    @Test
    public void testFunctionDTOConstructionForMethodWithParameters() {
        Function<?> increment = FunctionTestsUtil.getFunctionFactory().createMethodWrappingFunction(incrementMethod);
        
        FunctionDTO expectedDTO = createFunctionDTOWithParameters();
        assertThat(FunctionTestsUtil.getFunctionDTOFactory().createFunctionDTO(increment, Locale.ENGLISH, stringMessages), is(expectedDTO));
        assertThat(FunctionTestsUtil.getFunctionDTOFactory().createFunctionDTO(increment, Locale.GERMAN, stringMessages), is(expectedDTO));
    }

    private FunctionDTO createFunctionDTOWithParameters() {
        String functionName = incrementMethod.getName();
        String sourceTypeName = incrementMethod.getDeclaringClass().getSimpleName();
        String returnTypeName = incrementMethod.getReturnType().getSimpleName();
        List<String> parameterTypeNames = new ArrayList<>();
        parameterTypeNames.add(int.class.getSimpleName());

        String displayName = functionName;
        boolean isDimension = false;
        int ordinal = 0;
        
        return new FunctionDTOImpl(isDimension, functionName, sourceTypeName, returnTypeName, parameterTypeNames, displayName, ordinal);
    }
    
    @Test
    public void testThatLocalizedFunctionDTOAndUnlocalizedAreEqual() {
        Function<?> dimension = FunctionTestsUtil.getFunctionFactory().createMethodWrappingFunction(dimensionMethod);
        
        FunctionDTO unlocalizedDimensionDTO = FunctionTestsUtil.getFunctionDTOFactory().createFunctionDTO(dimension);
        FunctionDTO englishDimensionDTO = FunctionTestsUtil.getFunctionDTOFactory().createFunctionDTO(dimension, Locale.ENGLISH, TestsUtil.getTestStringMessages());
        FunctionDTO germanDimensionDTO = FunctionTestsUtil.getFunctionDTOFactory().createFunctionDTO(dimension, Locale.GERMAN, TestsUtil.getTestStringMessages());
        
        assertThat(unlocalizedDimensionDTO, is(englishDimensionDTO));
        assertThat(englishDimensionDTO, is(germanDimensionDTO));
    }

}
