package com.sap.sailing.gwt.ui.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.impl.BoatImpl;
import com.sap.sse.common.Util;
import com.sap.sse.common.filter.AbstractListFilter;

public class AbstractListFilterTest {

    AbstractListFilter<BoatImpl> als = new AbstractListFilter<BoatImpl>() {
        @Override
        public Iterable<String> getStrings(BoatImpl t) {
            return Arrays.asList(t.getName(), t.getSailID());
        }
    };
    ArrayList<BoatImpl> all = new ArrayList<BoatImpl>();

    @Before
    public void setUp() {
        all.add(new BoatImpl("1", "Race 1", null, "Kiel"));
        all.add(new BoatImpl("2", "Race 2", null, "Kiel"));
        all.add(new BoatImpl("3", "Race 3", null, "Kiel"));
        all.add(new BoatImpl("4", "Race 1", null, "Wannsee"));
        all.add(new BoatImpl("5", "Race 2", null, "Wannsee"));
        all.add(new BoatImpl("6", "Final", null, "Wannsee"));
    }

    @Test
    public void test() {
        assertEquals(2, Util.size(als.applyFilter(Arrays.asList(new String[] { "Race", "2" }), all)));
        assertEquals(1, Util.size(als.applyFilter(Collections.singleton("3"), all)));
        assertEquals("Race 3", als.applyFilter(Collections.singleton("3"), all).iterator().next().getName());
        assertEquals(1, Util.size(als.applyFilter(Arrays.asList(new String[] { "1", "Wannsee" }), all)));
        assertEquals("Race 1", als.applyFilter(Arrays.asList(new String[] { "1", "Wannsee" }), all).iterator().next().getName());
        assertEquals(0, Util.size(als.applyFilter(Arrays.asList(new String[] { "Wannsee", "3" }), all)));
        assertEquals(5, Util.size(als.applyFilter(Arrays.asList(new String[] { "Race", "Race" }), all)));
        assertEquals(5, Util.size(als.applyFilter(Collections.singleton("R"), all)));
        assertEquals(4, Util.size(als.applyFilter(Collections.singleton("i"), all)));
    }
}
