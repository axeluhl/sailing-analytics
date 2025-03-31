package com.sap.sse.test;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class VariableArityParameterNeverNullTest {
    @Test
    public void callVariableArityWithoutActualValue() {
        m();
    }
    
    private void m(String... variableArityFormalParam) {
        assertNotNull(variableArityFormalParam);
    }
}
