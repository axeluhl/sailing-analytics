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
import com.sap.sailing.datamining.test.function.test_classes.SimpleClassWithMarkedMethods;

public class TestSimpleFunctionRegistry {

    @Test
    public void testSimpleRegistration() {
        Method dimension = null;
        try {
            dimension = SimpleClassWithMarkedMethods.class.getMethod("dimension", (Class<?>[]) null);
        } catch (NoSuchMethodException | SecurityException e) {
            fail("Failed to reflect the method");
        }
        
        FunctionRegistry registry = new SimpleFunctionRegistry();
        registry.register(dimension);
        
        Set<Function> expectedRegisteredFunctionsAsSet = new HashSet<>();
        expectedRegisteredFunctionsAsSet.add(new MethodWrappingFunction(dimension));
        Iterable<Function> expectedRegisteredFunctions = expectedRegisteredFunctionsAsSet;
        assertThat(registry.getAllRegisteredFunctions(), is(expectedRegisteredFunctions));
        assertThat(registry.getRegisteredFunctionsOf(SimpleClassWithMarkedMethods.class), is(expectedRegisteredFunctions));
    }
    
    // TODO Test that inherited marked methods are also registered

    // TODO Test the registration of libraries

}
