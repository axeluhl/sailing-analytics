package com.sap.sse.datamining.impl.criterias;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;

import com.sap.sse.datamining.components.FilterCriteria;
import com.sap.sse.datamining.data.ClusterOfComparable;
import com.sap.sse.datamining.factories.FunctionFactory;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.impl.criterias.deprecated.SimpleRangeFilterCriteria;
import com.sap.sse.datamining.impl.data.ClusterOfComparableImpl;
import com.sap.sse.datamining.test.function.test_classes.DataTypeWithContext;
import com.sap.sse.datamining.test.function.test_classes.DataTypeWithContextImpl;
import com.sap.sse.datamining.test.util.FunctionTestsUtil;
import com.sap.sse.datamining.test.util.StringRegexFilterCriteria;

public class TestFilterCriterias {

    @Test
    public void testRegexFilterCriteria() {
        FilterCriteria<String> regexFilterCriteria = new StringRegexFilterCriteria(".*");
        assertTrue(regexFilterCriteria.matches("some Random stuff"));
        assertFalse(regexFilterCriteria.matches(null));
        
        String[] stringsToMatch = new String[] {"Regatta", "Other Regatta", "Third Regatta"};
        
        regexFilterCriteria = new StringRegexFilterCriteria(stringsToMatch[0] + "|" + stringsToMatch[1] + "|" + stringsToMatch[2]);
        for (String stringToMatch : stringsToMatch) {
            assertTrue("Failed to match " + stringToMatch, regexFilterCriteria.matches(stringToMatch));
        }
        assertFalse("'Fourth Regatta' shouldn't be matched", regexFilterCriteria.matches("Fourth Regatta"));
        
        regexFilterCriteria = new StringRegexFilterCriteria(".*Regatta");
        for (String stringToMatch : stringsToMatch) {
            assertTrue("Failed to match " + stringToMatch, regexFilterCriteria.matches(stringToMatch));
        }
        assertFalse("'Regatta (29ER)' shouldn't be matched", regexFilterCriteria.matches("Regatta (29ER)"));
    }
    
    @Test
    public void testRangeFilterCriteria() {
        ClusterOfComparable<Integer> cluster = new ClusterOfComparableImpl<Integer>("Test", 3, 1);
        FilterCriteria<Integer> rangeFilterCriteria = new SimpleRangeFilterCriteria<Integer>(cluster);

        assertTrue(rangeFilterCriteria.matches(1));
        assertTrue(rangeFilterCriteria.matches(2));
        assertTrue(rangeFilterCriteria.matches(3));

        assertFalse(rangeFilterCriteria.matches(0));
        assertFalse(rangeFilterCriteria.matches(4));
    }
    
    @Test
    public void testCompoundFilterCriterias() {
        StringRegexFilterCriteria startsWithBar = new StringRegexFilterCriteria("Bar.*");
        StringRegexFilterCriteria endWithFoo = new StringRegexFilterCriteria(".*Foo");
        
        CompoundFilterCriteria<String> compoundCriteria = new AndCompoundFilterCriteria<String>();
        compoundCriteria.addCriteria(startsWithBar);
        compoundCriteria.addCriteria(endWithFoo);
        
        assertTrue(compoundCriteria.matches("BarAndFoo"));
        assertFalse(compoundCriteria.matches("BarFo"));
        
        compoundCriteria = new OrCompoundFilterCriteria<String>();
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
        FilterCriteria<DataTypeWithContext> nullaryFunctionFilterCriteria = new NullaryFunctionValuesFilterCriteria<>(getRegattaName, valuesToMatch);
        
        DataTypeWithContext regatta = new DataTypeWithContextImpl("Regatta", "Race Name", 7);
        assertThat(nullaryFunctionFilterCriteria.matches(regatta), is(true));
        
        DataTypeWithContext otherRegatta = new DataTypeWithContextImpl("Other Regatta", "Race Name", 7);
        assertThat(nullaryFunctionFilterCriteria.matches(otherRegatta), is(true));
        
        DataTypeWithContext unmatchingRegatta = new DataTypeWithContextImpl("Unmatching Regatta", "Race Name", 7);
        assertThat(nullaryFunctionFilterCriteria.matches(unmatchingRegatta), is(false));
    }
    
}
