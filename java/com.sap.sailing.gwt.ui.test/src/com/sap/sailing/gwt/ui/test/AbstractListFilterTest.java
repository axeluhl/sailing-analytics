package com.sap.sailing.gwt.ui.test;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.impl.BoatImpl;
import com.sap.sailing.gwt.ui.client.shared.filter.AbstractListFilter;

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
        all.add(new BoatImpl("Race 1", null, "Kiel"));
        all.add(new BoatImpl("Race 2", null, "Kiel"));
        all.add(new BoatImpl("Race 3", null, "Kiel"));
        all.add(new BoatImpl("Race 1", null, "Wannsee"));
        all.add(new BoatImpl("Race 2", null, "Wannsee"));
        all.add(new BoatImpl("Final", null, "Wannsee"));
    }

    @Test
    public void test() {
        assertTrue(als.applyFilter("Race 2", all).size() == 2);
        assertTrue(als.applyFilter("3", all).size() == 1 && als.applyFilter("3", all).get(0).getName() == "Race 3");
        assertTrue(als.applyFilter("1 Wannsee", all).size() == 1 && als.applyFilter("1 Wannsee", all).get(0).getName() == "Race 1");
        assertTrue(als.applyFilter("Wannsee 3", all).size() == 0);
        assertTrue(als.applyFilter("Race Race", all).size() == 5);
        assertTrue(als.applyFilter("R", all).size() == 5);
        assertTrue(als.applyFilter("i", all).size() == 4);
    }
}
