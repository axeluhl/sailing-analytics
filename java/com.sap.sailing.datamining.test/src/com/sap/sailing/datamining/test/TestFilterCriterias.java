package com.sap.sailing.datamining.test;

import static org.junit.Assert.*;

import org.junit.Test;

import com.sap.sailing.datamining.FilterCriteria;
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
        
        regexFilterCriteria = new StringRegexFilterCriteria(".*Regatta");
        for (String stringToMatch : stringsToMatch) {
            assertTrue("Failed to match " + stringToMatch, regexFilterCriteria.matches(stringToMatch));
        }
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
