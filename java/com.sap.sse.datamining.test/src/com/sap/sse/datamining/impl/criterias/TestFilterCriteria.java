package com.sap.sse.datamining.impl.criterias;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;

import com.sap.sse.datamining.components.FilterCriterion;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.test.functions.test_classes.DataTypeWithContext;
import com.sap.sse.datamining.test.functions.test_classes.DataTypeWithContextImpl;
import com.sap.sse.datamining.test.util.FunctionTestsUtil;
import com.sap.sse.datamining.test.util.StringRegexFilterCriterion;

public class TestFilterCriteria {
    
    @Test
    public void testNonFilteringFilterCriterion() {
        FilterCriterion<String> nonFilteringFilterCriterion = new NonFilteringFilterCriterion<>(String.class);
        assertTrue(nonFilteringFilterCriterion.matches("Test"));
        assertTrue(nonFilteringFilterCriterion.matches(""));
        assertTrue(nonFilteringFilterCriterion.matches(null));
    }

    @Test
    public void testRegexFilterCriterion() {
        FilterCriterion<String> regexFilterCriterion = new StringRegexFilterCriterion(".*");
        assertTrue(regexFilterCriterion.matches("some Random stuff"));
        assertFalse(regexFilterCriterion.matches(null));
        
        String[] stringsToMatch = new String[] {"Regatta", "Other Regatta", "Third Regatta"};
        
        regexFilterCriterion = new StringRegexFilterCriterion(stringsToMatch[0] + "|" + stringsToMatch[1] + "|" + stringsToMatch[2]);
        for (String stringToMatch : stringsToMatch) {
            assertTrue("Failed to match " + stringToMatch, regexFilterCriterion.matches(stringToMatch));
        }
        assertFalse("'Fourth Regatta' shouldn't be matched", regexFilterCriterion.matches("Fourth Regatta"));
        
        regexFilterCriterion = new StringRegexFilterCriterion(".*Regatta");
        for (String stringToMatch : stringsToMatch) {
            assertTrue("Failed to match " + stringToMatch, regexFilterCriterion.matches(stringToMatch));
        }
        assertFalse("'Regatta (29ER)' shouldn't be matched", regexFilterCriterion.matches("Regatta (29ER)"));
    }
    
    @Test
    public void testCompoundFilterCriterion() {
        StringRegexFilterCriterion startsWithBar = new StringRegexFilterCriterion("Bar.*");
        StringRegexFilterCriterion endWithFoo = new StringRegexFilterCriterion(".*Foo");
        
        CompoundFilterCriterion<String> compoundCriterion = new AndCompoundFilterCriterion<String>(String.class);
        compoundCriterion.addCriteria(startsWithBar);
        compoundCriterion.addCriteria(endWithFoo);
        
        assertTrue(compoundCriterion.matches("BarAndFoo"));
        assertFalse(compoundCriterion.matches("BarFo"));
        
        compoundCriterion = new OrCompoundFilterCriterion<String>(String.class);
        compoundCriterion.addCriteria(startsWithBar);
        compoundCriterion.addCriteria(endWithFoo);

        assertTrue(compoundCriterion.matches("BarF"));
        assertTrue(compoundCriterion.matches("BraFoo"));
        assertFalse(compoundCriterion.matches("Nothing"));
    }
    
    @Test
    public void testNullaryFunctionValuesFilterCriterion() {
        Function<String> getRegattaName = FunctionTestsUtil.getFunctionFactory().createMethodWrappingFunction(FunctionTestsUtil.getMethodFromClass(DataTypeWithContext.class, "getRegattaName"));
        Collection<String> valuesToMatch = Arrays.asList("Regatta", "Other Regatta");
        FilterCriterion<DataTypeWithContext> nullaryFunctionFilterCriterion = new FunctionValuesFilterCriterion<>(DataTypeWithContext.class, getRegattaName, valuesToMatch);
        
        DataTypeWithContext regatta = new DataTypeWithContextImpl("Regatta", "Race Name", 7);
        assertThat(nullaryFunctionFilterCriterion.matches(regatta), is(true));
        
        DataTypeWithContext otherRegatta = new DataTypeWithContextImpl("Other Regatta", "Race Name", 7);
        assertThat(nullaryFunctionFilterCriterion.matches(otherRegatta), is(true));
        
        DataTypeWithContext unmatchingRegatta = new DataTypeWithContextImpl("Unmatching Regatta", "Race Name", 7);
        assertThat(nullaryFunctionFilterCriterion.matches(unmatchingRegatta), is(false));
    }
    
}
