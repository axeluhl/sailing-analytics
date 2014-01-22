package com.sap.sailing.datamining.function;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.sap.sailing.datamining.function.impl.MethodWrappingFunction;
import com.sap.sailing.datamining.function.impl.SimpleFunctionRegistry;
import com.sap.sailing.datamining.test.function.test_classes.ClassWithMarkedMethods;

public class TestSimpleFunctionRegistry {

    @Test
    public void testSimpleRegistration() {
        Method dimension = null;
        try {
            dimension = ClassWithMarkedMethods.class.getMethod("dimension", (Class<?>[]) null);
        } catch (NoSuchMethodException | SecurityException e) {
            fail("Failed to reflect the method");
        }
        
        FunctionRegistry registry = new SimpleFunctionRegistry();
        registry.register(dimension);
        
        Set<Function> expectedRegisteredFunctionsAsSet = new HashSet<>();
        expectedRegisteredFunctionsAsSet.add(new MethodWrappingFunction(dimension));
        Iterable<Function> expectedRegisteredFunctions = expectedRegisteredFunctionsAsSet;
        assertThat(registry.getAllRegisteredFunctions(), is(expectedRegisteredFunctions));
        assertThat(registry.getRegisteredFunctionsOf(ClassWithMarkedMethods.class), is(expectedRegisteredFunctions));
    }

    private Set<Function> getExpectedRegisteredMethodsToTestMarkedMethods() {
        Set<Function> expectedRegisteredFunctions = new HashSet<>();
        try {
            Method dimension = ClassWithMarkedMethods.class.getMethod("dimension", (Class<?>[]) null);
            Method sideEffectFreeValue = ClassWithMarkedMethods.class.getMethod("sideEffectFreeValue", (Class<?>[]) null);
            expectedRegisteredFunctions.add(new MethodWrappingFunction(dimension));
            expectedRegisteredFunctions.add(new MethodWrappingFunction(sideEffectFreeValue));
        } catch (NoSuchMethodException | SecurityException e) {
            fail("Failed to get the marked methods with reflection. Have the names been changed?");
        }
        return expectedRegisteredFunctions;
    }
    
    // TODO Test that inherited marked methods are also registered

    // TODO Test the registration of libraries

}
