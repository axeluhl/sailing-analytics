package com.sap.sse.datamining.impl.criterias;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;

import com.sap.sse.datamining.components.FilterCriterion;
import com.sap.sse.datamining.factories.FunctionFactory;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.test.functions.test_classes.DataTypeWithContext;
import com.sap.sse.datamining.test.functions.test_classes.DataTypeWithContextImpl;
import com.sap.sse.datamining.test.util.FunctionTestsUtil;
import com.sap.sse.datamining.test.util.StringRegexFilterCriterion;

public class TestFilterCriterias {

    @Test
    public void testRegexFilterCriteria() {
        FilterCriterion<String> regexFilterCriteria = new StringRegexFilterCriterion(".*");
        assertTrue(regexFilterCriteria.matches("some Random stuff"));
        assertFalse(regexFilterCriteria.matches(null));
        
        String[] stringsToMatch = new String[] {"Regatta", "Other Regatta", "Third Regatta"};
        
        regexFilterCriteria = new StringRegexFilterCriterion(stringsToMatch[0] + "|" + stringsToMatch[1] + "|" + stringsToMatch[2]);
        for (String stringToMatch : stringsToMatch) {
            assertTrue("Failed to match " + stringToMatch, regexFilterCriteria.matches(stringToMatch));
        }
        assertFalse("'Fourth Regatta' shouldn't be matched", regexFilterCriteria.matches("Fourth Regatta"));
        
        regexFilterCriteria = new StringRegexFilterCriterion(".*Regatta");
        for (String stringToMatch : stringsToMatch) {
            assertTrue("Failed to match " + stringToMatch, regexFilterCriteria.matches(stringToMatch));
        }
        assertFalse("'Regatta (29ER)' shouldn't be matched", regexFilterCriteria.matches("Regatta (29ER)"));
    }
    
    @Test
    public void testCompoundFilterCriterias() {
        StringRegexFilterCriterion startsWithBar = new StringRegexFilterCriterion("Bar.*");
        StringRegexFilterCriterion endWithFoo = new StringRegexFilterCriterion(".*Foo");
        
        CompoundFilterCriterion<String> compoundCriteria = new AndCompoundFilterCriterion<String>(String.class);
        compoundCriteria.addCriteria(startsWithBar);
        compoundCriteria.addCriteria(endWithFoo);
        
        assertTrue(compoundCriteria.matches("BarAndFoo"));
        assertFalse(compoundCriteria.matches("BarFo"));
        
        compoundCriteria = new OrCompoundFilterCriterion<String>(String.class);
        compoundCriteria.addCriteria(startsWithBar);
        compoundCriteria.addCriteria(endWithFoo);

        assertTrue(compoundCriteria.matches("BarF"));
        assertTrue(compoundCriteria.matches("BraFoo"));
        assertFalse(compoundCriteria.matches("Nothing"));
    }
    
    @Test
    public void testNullaryFunctionValuesFilterCriteria() {
        Function<String> getRegattaName = FunctionFactory.createMethodWrappingFunction(FunctionTestsUtil.getMethodFromClass(DataTypeWithContext.class, "getRegattaName"));
        Collection<String> valuesToMatch = Arrays.asList("Regatta", "Other Regatta");
        FilterCriterion<DataTypeWithContext> nullaryFunctionFilterCriteria = new NullaryFunctionValuesFilterCriterion<>(DataTypeWithContext.class, getRegattaName, valuesToMatch);
        
        DataTypeWithContext regatta = new DataTypeWithContextImpl("Regatta", "Race Name", 7);
        assertThat(nullaryFunctionFilterCriteria.matches(regatta), is(true));
        
        DataTypeWithContext otherRegatta = new DataTypeWithContextImpl("Other Regatta", "Race Name", 7);
        assertThat(nullaryFunctionFilterCriteria.matches(otherRegatta), is(true));
        
        DataTypeWithContext unmatchingRegatta = new DataTypeWithContextImpl("Unmatching Regatta", "Race Name", 7);
        assertThat(nullaryFunctionFilterCriteria.matches(unmatchingRegatta), is(false));
    }
    
}
