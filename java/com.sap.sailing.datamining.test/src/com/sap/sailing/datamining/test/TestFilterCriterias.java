package com.sap.sailing.datamining.test;

import static org.junit.Assert.*;

import org.junit.Test;

import com.sap.sailing.datamining.ClusterOfComparable;
import com.sap.sailing.datamining.FilterCriteria;
import com.sap.sailing.datamining.impl.ClusterOfComparableImpl;
import com.sap.sailing.datamining.impl.criterias.RangeFilterCriteria;
import com.sap.sailing.datamining.impl.criterias.RegexFilterCriteria;

public class TestFilterCriterias {

    @Test
    public void testRegexFilterCriteria() {
        FilterCriteria<String> regexFilterCriteria = new StringRegexFilterCriteria(".*");
        assertTrue(regexFilterCriteria.matches("some Random stuff"));
        
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
        FilterCriteria<Integer> rangeFilterCriteria = new RangeFilterCriteria<Integer, Integer>(cluster) {
            @Override
            public Integer getValue(Integer data) {
                return data;
            }
        };

        assertTrue(rangeFilterCriteria.matches(1));
        assertTrue(rangeFilterCriteria.matches(2));
        assertTrue(rangeFilterCriteria.matches(3));

        assertFalse(rangeFilterCriteria.matches(0));
        assertFalse(rangeFilterCriteria.matches(4));
    }

    private class StringRegexFilterCriteria extends RegexFilterCriteria<String> {

        public StringRegexFilterCriteria(String regex) {
            super(regex);
        }

        @Override
        protected String getValueToMatch(String data) {
            return data;
        }

    }

}
