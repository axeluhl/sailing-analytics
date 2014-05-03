package com.sap.sse.datamining.test.util;

import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.sap.sse.datamining.factories.FunctionFactory;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.test.functions.test_classes.DataTypeInterface;
import com.sap.sse.datamining.test.functions.test_classes.DataTypeWithContext;
import com.sap.sse.datamining.test.functions.test_classes.DataTypeWithContextImpl;
import com.sap.sse.datamining.test.functions.test_classes.ExtendingInterface;
import com.sap.sse.datamining.test.functions.test_classes.ExternalLibraryClass;
import com.sap.sse.datamining.test.functions.test_classes.SimpleClassWithMarkedMethods;

public class FunctionTestsUtil extends ConcurrencyTestsUtil {
    
    public static Collection<Function<?>> getMarkedMethodsOfSimpleClassWithMarkedMethod() {
        Set<Function<?>> markedMethods = new HashSet<>();
        markedMethods.add(FunctionFactory.createMethodWrappingFunction(getMethodFromSimpleClassWithMarkedMethod("dimension")));
        markedMethods.add(FunctionFactory.createMethodWrappingFunction(getMethodFromSimpleClassWithMarkedMethod("sideEffectFreeValue")));
        return markedMethods;
    }
    
    public static Method getMethodFromSimpleClassWithMarkedMethod(String name) {
        return getMethodFromClass(SimpleClassWithMarkedMethods.class, name);
    }

    public static Collection<Function<?>> getMethodsOfExternalLibraryClass() {
        Set<Function<?>> markedMethods = new HashSet<>();
        markedMethods.add(FunctionFactory.createMethodWrappingFunction(getMethodFromExternalLibraryClass("foo")));
        return markedMethods;
    }

    public static Method getMethodFromExternalLibraryClass(String name) {
        return getMethodFromClass(ExternalLibraryClass.class, name);
    }

    public static Collection<Function<?>> getMarkedMethodsOfDataTypeWithContextImplAndItsSupertypes() {
        Collection<Function<?>> markedMethods = getMarkedMethodsOfDataTypeWithContextAndItsSupertypes();
        markedMethods.add(FunctionFactory.createMethodWrappingFunction(getMethodFromClass(ExtendingInterface.class, "getRaceNameLength")));
        return markedMethods;
    }

    public static Collection<Function<?>> getMarkedMethodsOfDataTypeWithContextAndItsSupertypes() {
        Collection<Function<?>> markedMethods = new HashSet<>();
        markedMethods.add(FunctionFactory.createMethodWrappingFunction(getMethodFromClass(DataTypeInterface.class, "getSpeedInKnots")));
        markedMethods.add(FunctionFactory.createMethodWrappingFunction(getMethodFromClass(DataTypeWithContext.class, "getRegattaName")));
        markedMethods.add(FunctionFactory.createMethodWrappingFunction(getMethodFromClass(DataTypeWithContext.class, "getRaceName")));
        markedMethods.add(FunctionFactory.createMethodWrappingFunction(getMethodFromClass(DataTypeWithContext.class, "getLegNumber")));
        return markedMethods;
    }
    
    public static Method getMethodFromClass(Class<?> fromClass, String methodName) {
        return getMethodFromClass(fromClass, methodName, (Class<?>[]) null);
    }

    public static Method getMethodFromClass(Class<?> fromClass, String methodName, Class<?>... parameterTypes) {
        Method method = null;
        try {
            method = fromClass.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException | SecurityException e) {
            fail("Failed to get the method with name '" + methodName + "' from class '" + fromClass.getSimpleName() + "'. Have the names been changed?");
        }
        return method;
    }

    public static Collection<Function<?>> getDimensionsFor(Class<?> dataType) {
        if (dataType.equals(DataTypeWithContext.class) || dataType.equals(DataTypeWithContextImpl.class)) {
            return getDimensionsForDataTypeWithContext();
        }
        if (dataType.equals(SimpleClassWithMarkedMethods.class)) {
            return getDimensionsForSimpleClassWithMarkedMethods();
        }
        throw new IllegalArgumentException("There are no dimensions for " + dataType.getSimpleName());
    }

    private static Collection<Function<?>> getDimensionsForSimpleClassWithMarkedMethods() {
        Collection<Function<?>> dimensions = new HashSet<>();
        dimensions.add(FunctionFactory.createMethodWrappingFunction(getMethodFromSimpleClassWithMarkedMethod("dimension")));
        return dimensions;
    }

    private static Collection<Function<?>> getDimensionsForDataTypeWithContext() {
        Collection<Function<?>> dimensions = new HashSet<>();
        dimensions.add(FunctionFactory.createMethodWrappingFunction(getMethodFromClass(DataTypeWithContext.class, "getRegattaName")));
        dimensions.add(FunctionFactory.createMethodWrappingFunction(getMethodFromClass(DataTypeWithContext.class, "getRaceName")));
        dimensions.add(FunctionFactory.createMethodWrappingFunction(getMethodFromClass(DataTypeWithContext.class, "getLegNumber")));
        return dimensions;
    }
    
    private FunctionTestsUtil() {
    }

}
