package com.sap.sailing.datamining.function;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.sap.sailing.datamining.function.impl.MethodWrappingFunction;
import com.sap.sailing.datamining.function.impl.SimpleFunctionRegistry;
import com.sap.sailing.datamining.test.function.test_classes.SimpleClassWithMarkedMethods;
import com.sap.sailing.datamining.test.util.FunctionTestsUtil;

public class TestSimpleFunctionRegistry {

    @Test
    public void testSimpleRegistration() {
        Method dimension = FunctionTestsUtil.getMethodFromSimpleClassWithMarkedMethod("dimension");
        
        FunctionRegistry registry = new SimpleFunctionRegistry();
        registry.register(dimension);
        
        Set<Function> expectedRegisteredFunctionsAsSet = new HashSet<>();
        expectedRegisteredFunctionsAsSet.add(new MethodWrappingFunction(dimension));
        Iterable<Function> expectedRegisteredFunctions = expectedRegisteredFunctionsAsSet;
        assertThat(registry.getAllRegisteredFunctions(), is(expectedRegisteredFunctions));
        assertThat(registry.getRegisteredFunctionsOf(SimpleClassWithMarkedMethods.class), is(expectedRegisteredFunctions));
    }
    
//    @Test
//    public void testRegistrationByMethodRetrievers() {
//        
//    }

}
