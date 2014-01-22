package com.sap.sailing.datamining.test.util;

import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.datamining.function.Function;
import com.sap.sailing.datamining.function.impl.MethodWrappingFunction;
import com.sap.sailing.datamining.test.function.test_classes.SimpleClassWithMarkedMethods;

public class TestFunctionUtil {

    public static Collection<Function> getMarkedMethodsOfSimpleClassWithMarkedMethod() {
        Set<Function> expectedRegisteredFunctions = new HashSet<>();
        expectedRegisteredFunctions.add(new MethodWrappingFunction(getMethodFromSimpleClassWithMarkedMethod("dimension")));
        expectedRegisteredFunctions.add(new MethodWrappingFunction(getMethodFromSimpleClassWithMarkedMethod("sideEffectFreeValue")));
        return expectedRegisteredFunctions;
    }
    
    public static Method getMethodFromSimpleClassWithMarkedMethod(String name) {
        Method method = null;
        try {
            method = SimpleClassWithMarkedMethods.class.getMethod(name, (Class<?>[]) null);
        } catch (NoSuchMethodException | SecurityException e) {
            fail("Failed to get the method with name '" + name + "'. Have the names been changed?");
        }
        return method;
    }

}
