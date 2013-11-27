package com.sap.sailing.gwt.ui.test;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.gwt.ui.client.shared.filter.AbstractListFilter;

public class AbstractListFilterTest {
    
    AbstractListFilter<String> als = new AbstractListFilter<String>(){
        @Override
        public Iterable<String> getStrings(String t) {
            return Arrays.asList(t);
        }
    };
    ArrayList<String> list = new ArrayList<String>();

    @Before
    public void setUp() throws Exception {
        list.add("Race 1");
        list.add("Race 2");
    }

    @Test
    public void test() {
        assertTrue(!als.applyFilter(Arrays.asList("Race 2"), list).contains("Race 1"));
    }
}
