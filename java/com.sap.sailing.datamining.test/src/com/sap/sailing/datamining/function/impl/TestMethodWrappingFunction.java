package com.sap.sailing.datamining.function.impl;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.datamining.DataMiningStringMessages;
import com.sap.sailing.datamining.factories.FunctionFactory;
import com.sap.sailing.datamining.function.Function;
import com.sap.sailing.datamining.shared.Message;
import com.sap.sailing.datamining.test.function.test_classes.ExternalLibraryClass;
import com.sap.sailing.datamining.test.function.test_classes.SimpleClassWithMarkedMethods;
import com.sap.sailing.datamining.test.util.FunctionTestsUtil;

public class TestMethodWrappingFunction {
    
    private Method dimensionMethod;
    private Method sideEffectFreeValueMethod;
    private Method externalLibraryMethod;

    @Before
    public void initializeMethods() {
        dimensionMethod = FunctionTestsUtil.getMethodFromClass(SimpleClassWithMarkedMethods.class, "dimension");
        sideEffectFreeValueMethod = FunctionTestsUtil.getMethodFromClass(SimpleClassWithMarkedMethods.class, "sideEffectFreeValue");
        externalLibraryMethod = FunctionTestsUtil.getMethodFromClass(ExternalLibraryClass.class, "foo");
    }
    
//    @Test(expected=IllegalArgumentException.class)
//    public void testMethodReturnTypeAndGivenReturnTypeDoesntMatch() {
//        @SuppressWarnings("unused")
//        Function<String> function = new MethodWrappingFunction<>(externalLibraryMethod, String.class);
//    }
    
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
        StringMessages stringMessages = new StringMessages();

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
    
    private class StringMessages implements DataMiningStringMessages {
        
        private final Map<Locale, Map<String, String>> messages;
        
        public StringMessages() {
            messages = new HashMap<>();
            initializeEnglishMessages();
            initializeGermanMessages();
        }

        private void initializeEnglishMessages() {
            Map<String, String> englishMessages = new HashMap<>();
            messages.put(Locale.ENGLISH, englishMessages);

            englishMessages.put("dimension", "dimension-english");
            englishMessages.put("value", "sideEffectFreeValue-english");
        }

        private void initializeGermanMessages() {
            Map<String, String> germanMessages = new HashMap<>();
            messages.put(Locale.GERMAN, germanMessages);

            germanMessages.put("dimension", "dimension-deutsch");
            germanMessages.put("value", "sideEffectFreeValue-deutsch");
        }

        @Override
        public Locale getLocaleFrom(String localeName) {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public String get(Locale locale, Message message) {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public String get(Locale locale, Message message, String... parameters) {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public String get(Locale locale, Message message, Message... parameters) {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public String get(Locale locale, String messageKey) {
            return messages.get(locale).get(messageKey);
        }

        @Override
        public String get(Locale locale, String messageKey, String... parameters) {
            throw new UnsupportedOperationException("Not implemented");
        }
        
    }

}
