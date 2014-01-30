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
import com.sap.sailing.datamining.function.Function;
import com.sap.sailing.datamining.shared.dto.FunctionDTO;
import com.sap.sailing.datamining.shared.impl.dto.FunctionDTOImpl;
import com.sap.sailing.datamining.test.function.test_classes.ExternalLibraryClass;
import com.sap.sailing.datamining.test.function.test_classes.SimpleClassWithMarkedMethods;
import com.sap.sailing.datamining.test.util.FunctionTestsUtil;
import com.sap.sailing.datamining.test.util.StringMessagesForTests;

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
        
        FunctionDTO expectedDimensionDTO = createExpectedDimensionDTO(Locale.ENGLISH, "dimension");
        assertThat(dimension.asDTO(Locale.ENGLISH, stringMessages), is(expectedDimensionDTO));
        
        expectedDimensionDTO = createExpectedDimensionDTO(Locale.GERMAN, "dimension");
        assertThat(dimension.asDTO(Locale.GERMAN, stringMessages), is(expectedDimensionDTO));
    }

    public FunctionDTOImpl createExpectedDimensionDTO(Locale locale, String messageKey) {
        String functionName = dimensionMethod.getName();
        String sourceTypeName = dimensionMethod.getDeclaringClass().getSimpleName();
        String returnTypeName = dimensionMethod.getReturnType().getSimpleName();
        List<String> parameterTypeNames = new ArrayList<>();

        String displayName = stringMessages.get(locale, messageKey);
        boolean isDimension = true;
        
        return new FunctionDTOImpl(functionName, sourceTypeName, returnTypeName, parameterTypeNames, displayName, isDimension);
    }

}
