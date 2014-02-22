package com.sap.sse.datamining.impl.function;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;

import org.junit.Test;

import com.sap.sse.datamining.components.FilterCriteria;
import com.sap.sse.datamining.impl.functions.MethodIsCorrectDimensionFilterCriteria;
import com.sap.sse.datamining.impl.functions.MethodIsCorrectExternalFunctionFilterCriteria;
import com.sap.sse.datamining.impl.functions.MethodIsCorrectSideEffectFreeValueFilterCriteria;
import com.sap.sse.datamining.test.util.TestsUtil;

public class TestFunctionFilterCriterias {

    @Test
    public void testMethodIsCorrectDimensionFilterCriteria() {
        FilterCriteria<Method> filterCriteria = new MethodIsCorrectDimensionFilterCriteria();
        
        Method dimension = TestsUtil.getMethodFromSimpleClassWithMarkedMethod("dimension");
        assertThat(filterCriteria.matches(dimension), is(true));
        
        Method illegalDimension = TestsUtil.getMethodFromSimpleClassWithMarkedMethod("illegalDimension");
        assertThat(filterCriteria.matches(illegalDimension), is(false));
        
        Method unmarkedMethod = TestsUtil.getMethodFromSimpleClassWithMarkedMethod("unmarkedMethod");
        assertThat(filterCriteria.matches(unmarkedMethod), is(false));
    }

    @Test
    public void testMethodIsCorrectSideEffectFreeValueFilterCriteria() {
        FilterCriteria<Method> filterCriteria = new MethodIsCorrectSideEffectFreeValueFilterCriteria();
        
        Method sideEffectFreeValue = TestsUtil.getMethodFromSimpleClassWithMarkedMethod("sideEffectFreeValue");
        assertThat(filterCriteria.matches(sideEffectFreeValue), is(true));
        
        Method unmarkedMethod = TestsUtil.getMethodFromSimpleClassWithMarkedMethod("unmarkedMethod");
        assertThat(filterCriteria.matches(unmarkedMethod), is(false));
    }

    @Test
    public void testMethodIsCorrectExternalLibraryFunctionFilterCriteria() {
        FilterCriteria<Method> filterCriteria = new MethodIsCorrectExternalFunctionFilterCriteria();
        
        Method foo = TestsUtil.getMethodFromExternalLibraryClass("foo");
        assertThat(filterCriteria.matches(foo), is(true));
        
        Method fooVoid = TestsUtil.getMethodFromExternalLibraryClass("fooVoid");
        assertThat(filterCriteria.matches(fooVoid), is(false));
    }

}
