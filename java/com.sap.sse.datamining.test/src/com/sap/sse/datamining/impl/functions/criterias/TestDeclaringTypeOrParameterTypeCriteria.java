package com.sap.sse.datamining.impl.functions.criterias;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.datamining.components.FilterCriterion;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.test.functions.test_classes.DataTypeInterface;
import com.sap.sse.datamining.test.functions.test_classes.DataTypeWithContext;
import com.sap.sse.datamining.test.functions.test_classes.DataTypeWithContextImpl;
import com.sap.sse.datamining.test.functions.test_classes.ExtendingInterface;
import com.sap.sse.datamining.test.functions.test_classes.Test_ExternalLibraryClass;
import com.sap.sse.datamining.test.util.FunctionTestsUtil;

public class TestDeclaringTypeOrParameterTypeCriteria {

    private Function<?> getSpeedInKnotsValue;
    private Function<?> getRaceNameLengthValue;
    private Function<?> getRegattaNameDimension;
    
    private Function<?> libraryFunction;
    
    @Before
    public void setUpFunctions() {
        getSpeedInKnotsValue = FunctionTestsUtil.getFunctionFactory().createMethodWrappingFunction(FunctionTestsUtil.getMethodFromClass(DataTypeInterface.class, "getSpeedInKnots"));
        getRaceNameLengthValue = FunctionTestsUtil.getFunctionFactory().createMethodWrappingFunction(FunctionTestsUtil.getMethodFromClass(ExtendingInterface.class, "getRaceNameLength"));
        getRegattaNameDimension = FunctionTestsUtil.getFunctionFactory().createMethodWrappingFunction(FunctionTestsUtil.getMethodFromClass(DataTypeWithContext.class, "getRegattaName"));
        
        libraryFunction = FunctionTestsUtil.getFunctionFactory().createMethodWrappingFunction(FunctionTestsUtil.getMethodFromClass(Test_ExternalLibraryClass.class, "foo"));
    }

    @Test
    public void testMatchingTypeWithBigHierarchy() {
        FilterCriterion<Function<?>> criteria = new IsDeclaringTypeFilterCriterion(DataTypeWithContextImpl.class);

        assertThat(criteria.matches(getSpeedInKnotsValue), is(true));
        assertThat(criteria.matches(getRaceNameLengthValue), is(true));
        assertThat(criteria.matches(getRegattaNameDimension), is(true));

        assertThat(criteria.matches(libraryFunction), is(false));
    }
    
    @Test
    public void testGetElementType() {
        FilterCriterion<Function<?>> criteria = new IsDeclaringTypeFilterCriterion(DataTypeWithContextImpl.class);
        assertThat(criteria.getElementType().equals(Function.class), is(true));
    }

}
