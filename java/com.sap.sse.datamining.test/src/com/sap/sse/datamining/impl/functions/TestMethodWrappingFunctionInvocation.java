package com.sap.sse.datamining.impl.functions;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.test.data.impl.DataTypeWithContext;
import com.sap.sse.datamining.test.data.impl.DataTypeWithContextImpl;
import com.sap.sse.datamining.test.data.impl.SimpleClassWithMarkedMethods;
import com.sap.sse.datamining.test.util.FunctionTestsUtil;

public class TestMethodWrappingFunctionInvocation {
    
    private Function<String> getRegattaName;
    private Function<Integer> increment;

    @Before
    public void setUpFunctions() {
        getRegattaName = FunctionTestsUtil.getFunctionFactory().createMethodWrappingFunction(FunctionTestsUtil.getMethodFromClass(DataTypeWithContext.class, "getRegattaName"));
        increment = FunctionTestsUtil.getFunctionFactory().createMethodWrappingFunction(FunctionTestsUtil.getMethodFromClass(SimpleClassWithMarkedMethods.class, "increment", int.class));
    }

    @Test
    public void testInvocationWithNoParameters() {
        DataTypeWithContext dataEntry = new DataTypeWithContextImpl("Regatta Name", "Race Name", 7);
        assertThat(getRegattaName.tryToInvoke(dataEntry), is(dataEntry.getRegattaName()));
    }

    @Test
    public void testInvocationWithParameters() {
        SimpleClassWithMarkedMethods instance = new SimpleClassWithMarkedMethods();
        final int valueToIncrement = 10;
        assertThat(increment.tryToInvoke(instance, () -> {return new Object[] {valueToIncrement};} ), is(instance.increment(valueToIncrement)));
    }
    
    @Test
    public void testInvocationWithWrongParameters() {
        DataTypeWithContext dataEntry = new DataTypeWithContextImpl("Regatta Name", "Race Name", 7);
        assertThat(getRegattaName.tryToInvoke(dataEntry, () -> {return new Object[] {"Wrong Parameter"};} ), is(nullValue()));

        SimpleClassWithMarkedMethods instance = new SimpleClassWithMarkedMethods();
        assertThat(increment.tryToInvoke(instance), is(nullValue()));
    }

}
