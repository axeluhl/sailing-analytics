package com.sap.sailing.datamining.function.impl;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.datamining.factories.FunctionFactory;
import com.sap.sailing.datamining.function.Function;
import com.sap.sailing.datamining.test.function.test_classes.DataTypeWithContext;
import com.sap.sailing.datamining.test.function.test_classes.DataTypeWithContextImpl;
import com.sap.sailing.datamining.test.util.FunctionTestsUtil;

public class TestMethodWrappingFunctionInvocation {
    
    private Function<String> getRegattaName;

    @Before
    public void setUpFunctions() {
        getRegattaName = FunctionFactory.createMethodWrappingFunction(FunctionTestsUtil.getMethodFromClass(DataTypeWithContext.class, "getRegattaName"));
    }

    @Test
    public void testInvocationWithNoParameters() {
        DataTypeWithContext dataEntry = new DataTypeWithContextImpl("Regatta Name", "Race Name", 7);
        assertThat(getRegattaName.invoke(dataEntry), is(dataEntry.getRegattaName()));
    }

}
