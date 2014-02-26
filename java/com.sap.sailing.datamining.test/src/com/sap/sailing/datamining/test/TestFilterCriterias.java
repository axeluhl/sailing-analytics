package com.sap.sailing.datamining.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.sap.sailing.datamining.ClusterOfComparable;
import com.sap.sailing.datamining.ConcurrentFilterCriteria;
import com.sap.sailing.datamining.impl.ClusterOfComparableImpl;
import com.sap.sailing.datamining.impl.criterias.AndCompoundFilterCriteria;
import com.sap.sailing.datamining.impl.criterias.CompoundFilterCriteria;
import com.sap.sailing.datamining.impl.criterias.OrCompoundFilterCriteria;
import com.sap.sailing.datamining.impl.criterias.SimpleRangeFilterCriteria;
import com.sap.sailing.datamining.test.util.StringRegexFilterCriteria;

public class TestFilterCriterias {

    @Test
    public void testRegexFilterCriteria() {
        ConcurrentFilterCriteria<String> regexFilterCriteria = new StringRegexFilterCriteria(".*");
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
        ConcurrentFilterCriteria<Integer> rangeFilterCriteria = new SimpleRangeFilterCriteria<Integer>(cluster);

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

}
