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
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;
import com.sap.sse.datamining.test.functions.test_classes.SimpleClassWithMarkedMethods;
import com.sap.sse.datamining.test.functions.test_classes.Test_ExternalLibraryClass;
import com.sap.sse.datamining.test.util.FunctionTestsUtil;
import com.sap.sse.datamining.test.util.TestsUtil;
import com.sap.sse.i18n.ResourceBundleStringMessages;

public class TestFunctionDTOConstruction {
    
    private static final ResourceBundleStringMessages stringMessages = TestsUtil.getTestStringMessages();

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
        assertThat(FunctionTestsUtil.getFunctionDTOFactory().createFunctionDTO(dimension, stringMessages, Locale.ENGLISH), is(expectedDimensionDTO));
        assertThat(FunctionTestsUtil.getFunctionDTOFactory().createFunctionDTO(dimension, stringMessages, Locale.GERMAN), is(expectedDimensionDTO));
    }

    public FunctionDTO createExpectedDimensionDTO() {
        String functionName = dimensionMethod.getName();
        String sourceTypeName = dimensionMethod.getDeclaringClass().getSimpleName();
        String returnTypeName = dimensionMethod.getReturnType().getSimpleName();
        List<String> parameterTypeNames = new ArrayList<>();

        String displayName = functionName;
        boolean isDimension = true;
        int ordinal = 0;
        
        return new FunctionDTO(isDimension, functionName, sourceTypeName, returnTypeName, parameterTypeNames, displayName, ordinal);
    }
    
    @Test
    public void testNullarySideEffectFreeValueDTOConstruction() {
        Function<?> sideEffectFreeValue = FunctionTestsUtil.getFunctionFactory().createMethodWrappingFunction(sideEffectFreeValueMethod);
        
        FunctionDTO expectedDTO = createExpectedNullarySideEffectFreeValueDTO();
        assertThat(FunctionTestsUtil.getFunctionDTOFactory().createFunctionDTO(sideEffectFreeValue, stringMessages, Locale.ENGLISH), is(expectedDTO));
        assertThat(FunctionTestsUtil.getFunctionDTOFactory().createFunctionDTO(sideEffectFreeValue, stringMessages, Locale.GERMAN), is(expectedDTO));
    }

    private FunctionDTO createExpectedNullarySideEffectFreeValueDTO() {
        String functionName = sideEffectFreeValueMethod.getName();
        String sourceTypeName = sideEffectFreeValueMethod.getDeclaringClass().getSimpleName();
        String returnTypeName = sideEffectFreeValueMethod.getReturnType().getSimpleName();
        List<String> parameterTypeNames = new ArrayList<>();

        String displayName = functionName;
        boolean isDimension = false;
        int ordinal = 0;
        
        return new FunctionDTO(isDimension, functionName, sourceTypeName, returnTypeName, parameterTypeNames, displayName, ordinal);
    }
    
    @Test
    public void testExternalLibraryFunctionDTOConstruction() {
        Function<?> externalLibraryFunction = FunctionTestsUtil.getFunctionFactory().createMethodWrappingFunction(externalLibraryMethod);
        
        FunctionDTO expectedDTO = createExpectedExternalLibraryFunctionDTO();
        assertThat(FunctionTestsUtil.getFunctionDTOFactory().createFunctionDTO(externalLibraryFunction, stringMessages, Locale.ENGLISH), is(expectedDTO));
        assertThat(FunctionTestsUtil.getFunctionDTOFactory().createFunctionDTO(externalLibraryFunction, stringMessages, Locale.GERMAN), is(expectedDTO));
    }

    private FunctionDTO createExpectedExternalLibraryFunctionDTO() {
        String functionName = externalLibraryMethod.getName();
        String sourceTypeName = externalLibraryMethod.getDeclaringClass().getSimpleName();
        String returnTypeName = externalLibraryMethod.getReturnType().getSimpleName();
        List<String> parameterTypeNames = new ArrayList<>();

        String displayName = functionName;
        boolean isDimension = false;
        int ordinal = 0;
        
        return new FunctionDTO(isDimension, functionName, sourceTypeName, returnTypeName, parameterTypeNames, displayName, ordinal);
    }
    
    @Test
    public void testFunctionDTOConstructionForMethodWithParameters() {
        Function<?> increment = FunctionTestsUtil.getFunctionFactory().createMethodWrappingFunction(incrementMethod);
        
        FunctionDTO expectedDTO = createFunctionDTOWithParameters();
        assertThat(FunctionTestsUtil.getFunctionDTOFactory().createFunctionDTO(increment, stringMessages, Locale.ENGLISH), is(expectedDTO));
        assertThat(FunctionTestsUtil.getFunctionDTOFactory().createFunctionDTO(increment, stringMessages, Locale.GERMAN), is(expectedDTO));
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
        
        return new FunctionDTO(isDimension, functionName, sourceTypeName, returnTypeName, parameterTypeNames, displayName, ordinal);
    }
    
    @Test
    public void testThatLocalizedFunctionDTOAndUnlocalizedAreEqual() {
        Function<?> dimension = FunctionTestsUtil.getFunctionFactory().createMethodWrappingFunction(dimensionMethod);
        
        FunctionDTO unlocalizedDimensionDTO = FunctionTestsUtil.getFunctionDTOFactory().createFunctionDTO(dimension);
        FunctionDTO englishDimensionDTO = FunctionTestsUtil.getFunctionDTOFactory().createFunctionDTO(dimension, TestsUtil.getTestStringMessages(), Locale.ENGLISH);
        FunctionDTO germanDimensionDTO = FunctionTestsUtil.getFunctionDTOFactory().createFunctionDTO(dimension, TestsUtil.getTestStringMessages(), Locale.GERMAN);
        
        assertThat(unlocalizedDimensionDTO, is(englishDimensionDTO));
        assertThat(englishDimensionDTO, is(germanDimensionDTO));
    }

}
