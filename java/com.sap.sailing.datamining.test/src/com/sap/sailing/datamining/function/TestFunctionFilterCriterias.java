package com.sap.sailing.datamining.function;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;

import org.junit.Test;

import com.sap.sailing.datamining.ConcurrentFilterCriteria;
import com.sap.sailing.datamining.function.impl.MethodIsCorrectDimensionFilterCriteria;
import com.sap.sailing.datamining.function.impl.MethodIsCorrectSideEffectFreeValueFilterCriteria;
import com.sap.sailing.datamining.test.util.TestFunctionUtil;

public class TestFunctionFilterCriterias {

    @Test
    public void testMethodIsCorrectDimensionFilterCriteria() {
        ConcurrentFilterCriteria<Method> filterCriteria = new MethodIsCorrectDimensionFilterCriteria();
        
        Method dimension = TestFunctionUtil.getMethodFromSimpleClassWithMarkedMethod("dimension");
        assertThat(filterCriteria.matches(dimension), is(true));
        
        Method illegalDimension = TestFunctionUtil.getMethodFromSimpleClassWithMarkedMethod("illegalDimension");
        assertThat(filterCriteria.matches(illegalDimension), is(false));
        
        Method unmarkedMethod = TestFunctionUtil.getMethodFromSimpleClassWithMarkedMethod("unmarkedMethod");
        assertThat(filterCriteria.matches(unmarkedMethod), is(false));
    }

    @Test
    public void testMethodIsCorrectSideEffectFreeValueFilterCriteria() {
        ConcurrentFilterCriteria<Method> filterCriteria = new MethodIsCorrectSideEffectFreeValueFilterCriteria();
        
        Method sideEffectFreeValue = TestFunctionUtil.getMethodFromSimpleClassWithMarkedMethod("sideEffectFreeValue");
        assertThat(filterCriteria.matches(sideEffectFreeValue), is(true));
        
        Method unmarkedMethod = TestFunctionUtil.getMethodFromSimpleClassWithMarkedMethod("unmarkedMethod");
        assertThat(filterCriteria.matches(unmarkedMethod), is(false));
    }

}
