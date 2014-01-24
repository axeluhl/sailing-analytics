package com.sap.sailing.datamining.function;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;

import org.junit.Test;

import com.sap.sailing.datamining.ConcurrentFilterCriteria;
import com.sap.sailing.datamining.function.impl.MethodIsCorrectDimensionFilterCriteria;
import com.sap.sailing.datamining.function.impl.MethodIsCorrectExternalFunctionFilterCriteria;
import com.sap.sailing.datamining.function.impl.MethodIsCorrectSideEffectFreeValueFilterCriteria;
import com.sap.sailing.datamining.test.util.FunctionTestsUtil;

public class TestFunctionFilterCriterias {

    @Test
    public void testMethodIsCorrectDimensionFilterCriteria() {
        ConcurrentFilterCriteria<Method> filterCriteria = new MethodIsCorrectDimensionFilterCriteria();
        
        Method dimension = FunctionTestsUtil.getMethodFromSimpleClassWithMarkedMethod("dimension");
        assertThat(filterCriteria.matches(dimension), is(true));
        
        Method illegalDimension = FunctionTestsUtil.getMethodFromSimpleClassWithMarkedMethod("illegalDimension");
        assertThat(filterCriteria.matches(illegalDimension), is(false));
        
        Method unmarkedMethod = FunctionTestsUtil.getMethodFromSimpleClassWithMarkedMethod("unmarkedMethod");
        assertThat(filterCriteria.matches(unmarkedMethod), is(false));
    }

    @Test
    public void testMethodIsCorrectSideEffectFreeValueFilterCriteria() {
        ConcurrentFilterCriteria<Method> filterCriteria = new MethodIsCorrectSideEffectFreeValueFilterCriteria();
        
        Method sideEffectFreeValue = FunctionTestsUtil.getMethodFromSimpleClassWithMarkedMethod("sideEffectFreeValue");
        assertThat(filterCriteria.matches(sideEffectFreeValue), is(true));
        
        Method unmarkedMethod = FunctionTestsUtil.getMethodFromSimpleClassWithMarkedMethod("unmarkedMethod");
        assertThat(filterCriteria.matches(unmarkedMethod), is(false));
    }

    @Test
    public void testMethodIsCorrectExternalLibraryFunctionFilterCriteria() {
        ConcurrentFilterCriteria<Method> filterCriteria = new MethodIsCorrectExternalFunctionFilterCriteria();
        
        Method foo = FunctionTestsUtil.getMethodFromExternalLibraryClass("foo");
        assertThat(filterCriteria.matches(foo), is(true));
        
        Method fooVoid = FunctionTestsUtil.getMethodFromExternalLibraryClass("fooVoid");
        assertThat(filterCriteria.matches(fooVoid), is(false));
    }

}
