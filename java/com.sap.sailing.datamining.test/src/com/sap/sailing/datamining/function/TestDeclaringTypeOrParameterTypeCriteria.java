package com.sap.sailing.datamining.function;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.datamining.ConcurrentFilterCriteria;
import com.sap.sailing.datamining.function.impl.DeclaringTypeOrParameterTypeCriteria;
import com.sap.sailing.datamining.function.impl.MethodWrappingFunction;
import com.sap.sailing.datamining.test.function.test_classes.DataTypeInterface;
import com.sap.sailing.datamining.test.function.test_classes.DataTypeWithContext;
import com.sap.sailing.datamining.test.function.test_classes.DataTypeWithContextImpl;
import com.sap.sailing.datamining.test.function.test_classes.ExtendingInterface;
import com.sap.sailing.datamining.test.function.test_classes.ExternalLibraryClass;
import com.sap.sailing.datamining.test.util.FunctionTestsUtil;

public class TestDeclaringTypeOrParameterTypeCriteria {

    private Function getSpeedInKnotsValue;
    private Function getRaceNameLengthValue;
    private Function getRegattaNameDimension;
    
    private Function libraryFunction;
    
    @Before
    public void setUpFunctions() {
        getSpeedInKnotsValue = new MethodWrappingFunction(FunctionTestsUtil.getMethodFromClass(DataTypeInterface.class, "getSpeedInKnots"));
        getRaceNameLengthValue = new MethodWrappingFunction(FunctionTestsUtil.getMethodFromClass(ExtendingInterface.class, "getRaceNameLength"));
        getRegattaNameDimension = new MethodWrappingFunction(FunctionTestsUtil.getMethodFromClass(DataTypeWithContext.class, "getRegattaName"));
        
        libraryFunction = new MethodWrappingFunction(FunctionTestsUtil.getMethodFromClass(ExternalLibraryClass.class, "foo"));
    }

    @Test
    public void testMatchingTypeWithBigHierarchy() {
        ConcurrentFilterCriteria<Function> criteria = new DeclaringTypeOrParameterTypeCriteria(DataTypeWithContextImpl.class);

        assertThat(criteria.matches(getSpeedInKnotsValue), is(true));
        assertThat(criteria.matches(getRaceNameLengthValue), is(true));
        assertThat(criteria.matches(getRegattaNameDimension), is(true));

        assertThat(criteria.matches(libraryFunction), is(false));
    }

}
