package com.sap.sailing.datamining.test.util;

import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.datamining.function.Function;
import com.sap.sailing.datamining.function.impl.MethodWrappingFunction;
import com.sap.sailing.datamining.test.function.test_classes.SimpleClassWithMarkedMethods;

public class FunctionTestsUtil {

    public static Collection<Function> getMarkedMethodsOfSimpleClassWithMarkedMethod() {
        Set<Function> markedMethods = new HashSet<>();
        markedMethods.add(new MethodWrappingFunction(getMethodFromSimpleClassWithMarkedMethod("dimension")));
        markedMethods.add(new MethodWrappingFunction(getMethodFromSimpleClassWithMarkedMethod("sideEffectFreeValue")));
        return markedMethods;
    }
    
    public static Method getMethodFromSimpleClassWithMarkedMethod(String name) {
        return getMethodFromClass(SimpleClassWithMarkedMethods.class, name);
    }

    public static Collection<Function> getMethodsOfExternalLibraryClass() {
        Set<Function> markedMethods = new HashSet<>();
        markedMethods.add(new MethodWrappingFunction(getMethodFromExternalLibraryClass("foo")));
        return markedMethods;
    }

    public static Method getMethodFromExternalLibraryClass(String name) {
        return getMethodFromClass(ExternalLibraryClass.class, name);
    }
    
    private static Method getMethodFromClass(Class<?> fromClass, String methodName) {
        Method method = null;
        try {
            method = fromClass.getMethod(methodName, (Class<?>[]) null);
        } catch (NoSuchMethodException | SecurityException e) {
            fail("Failed to get the method with name '" + methodName + "'. Have the names been changed?");
        }
        return method;
    }

}
