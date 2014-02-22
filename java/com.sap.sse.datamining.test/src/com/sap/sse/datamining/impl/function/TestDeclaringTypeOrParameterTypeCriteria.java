package com.sap.sse.datamining.impl.function;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.datamining.components.FilterCriteria;
import com.sap.sse.datamining.factories.FunctionFactory;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.impl.functions.DeclaringTypeOrParameterTypeCriteria;
import com.sap.sse.datamining.test.function.test_classes.DataTypeInterface;
import com.sap.sse.datamining.test.function.test_classes.DataTypeWithContext;
import com.sap.sse.datamining.test.function.test_classes.DataTypeWithContextImpl;
import com.sap.sse.datamining.test.function.test_classes.ExtendingInterface;
import com.sap.sse.datamining.test.function.test_classes.ExternalLibraryClass;
import com.sap.sse.datamining.test.util.FunctionTestsUtil;

public class TestDeclaringTypeOrParameterTypeCriteria {

    private Function<?> getSpeedInKnotsValue;
    private Function<?> getRaceNameLengthValue;
    private Function<?> getRegattaNameDimension;
    
    private Function<?> libraryFunction;
    
    @Before
    public void setUpFunctions() {
        getSpeedInKnotsValue = FunctionFactory.createMethodWrappingFunction(FunctionTestsUtil.getMethodFromClass(DataTypeInterface.class, "getSpeedInKnots"));
        getRaceNameLengthValue = FunctionFactory.createMethodWrappingFunction(FunctionTestsUtil.getMethodFromClass(ExtendingInterface.class, "getRaceNameLength"));
        getRegattaNameDimension = FunctionFactory.createMethodWrappingFunction(FunctionTestsUtil.getMethodFromClass(DataTypeWithContext.class, "getRegattaName"));
        
        libraryFunction = FunctionFactory.createMethodWrappingFunction(FunctionTestsUtil.getMethodFromClass(ExternalLibraryClass.class, "foo"));
    }

    @Test
    public void testMatchingTypeWithBigHierarchy() {
        FilterCriteria<Function<?>> criteria = new DeclaringTypeOrParameterTypeCriteria(DataTypeWithContextImpl.class);

        assertThat(criteria.matches(getSpeedInKnotsValue), is(true));
        assertThat(criteria.matches(getRaceNameLengthValue), is(true));
        assertThat(criteria.matches(getRegattaNameDimension), is(true));

        assertThat(criteria.matches(libraryFunction), is(false));
    }

}
