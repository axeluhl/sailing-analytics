package com.sap.sse.datamining.impl.function;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.datamining.factories.FunctionFactory;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.impl.functions.MethodWrappingFunction;
import com.sap.sse.datamining.test.function.test_classes.ExternalLibraryClass;
import com.sap.sse.datamining.test.function.test_classes.SimpleClassWithMarkedMethods;
import com.sap.sse.datamining.test.util.TestsUtil;
import com.sap.sse.datamining.test.util.StringMessagesForTests;

public class TestMethodWrappingFunction {
    
    private Method dimensionMethod;
    private Method sideEffectFreeValueMethod;
    private Method externalLibraryMethod;

    @Before
    public void initializeMethods() {
        dimensionMethod = TestsUtil.getMethodFromClass(SimpleClassWithMarkedMethods.class, "dimension");
        sideEffectFreeValueMethod = TestsUtil.getMethodFromClass(SimpleClassWithMarkedMethods.class, "sideEffectFreeValue");
        externalLibraryMethod = TestsUtil.getMethodFromClass(ExternalLibraryClass.class, "foo");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testMethodReturnTypeAndGivenReturnTypeDoesntMatch() {
        @SuppressWarnings("unused")
        Function<String> function = new MethodWrappingFunction<>(externalLibraryMethod, String.class);
    }
    
    @Test
    public void testIsDimension() {
        Function<?> dimension = FunctionFactory.createMethodWrappingFunction(dimensionMethod);
        assertThat(dimension.isDimension(), is(true));

        Function<?> sideEffectFreeValue = FunctionFactory.createMethodWrappingFunction(sideEffectFreeValueMethod);
        assertThat(sideEffectFreeValue.isDimension(), is(false));

        Function<?> externalLibraryFunction = FunctionFactory.createMethodWrappingFunction(externalLibraryMethod);
        assertThat(externalLibraryFunction.isDimension(), is(false));
    }
    
    @Test
    public void testGetLocalizedName() {
        StringMessagesForTests stringMessages = new StringMessagesForTests();

        Function<?> dimension = FunctionFactory.createMethodWrappingFunction(dimensionMethod);
        String expectedEnglishName = "dimension-english";
        assertThat(dimension.getLocalizedName(Locale.ENGLISH, stringMessages), is(expectedEnglishName));
        String expectedGermanName = "dimension-deutsch";
        assertThat(dimension.getLocalizedName(Locale.GERMAN, stringMessages), is(expectedGermanName));

        Function<?> sideEffectFreeValue = FunctionFactory.createMethodWrappingFunction(sideEffectFreeValueMethod);
        expectedEnglishName = "sideEffectFreeValue-english";
        assertThat(sideEffectFreeValue.getLocalizedName(Locale.ENGLISH, stringMessages), is(expectedEnglishName));
        expectedGermanName = "sideEffectFreeValue-deutsch";
        assertThat(sideEffectFreeValue.getLocalizedName(Locale.GERMAN, stringMessages), is(expectedGermanName));

        Function<?> externalLibraryFunction = FunctionFactory.createMethodWrappingFunction(externalLibraryMethod);
        String expectedName = "foo";
        assertThat(externalLibraryFunction.getLocalizedName(Locale.ENGLISH, stringMessages), is(expectedName));
        assertThat(externalLibraryFunction.getLocalizedName(Locale.GERMAN, stringMessages), is(expectedName));
    }

}
