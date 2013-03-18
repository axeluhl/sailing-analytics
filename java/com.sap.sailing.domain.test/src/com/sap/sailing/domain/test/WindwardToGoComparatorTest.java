package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;

import java.util.Comparator;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Test;

public class WindwardToGoComparatorTest {
    @Before
    public void setUp() {
        
    }
    
    @Test
    public void testInsertEqualElementsIntoSortedSet() {
        TreeSet<Integer> ti = new TreeSet<Integer>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return 0;
            }
        });
        ti.add(new Integer(1));
        ti.add(new Integer(2));
        assertEquals(1, ti.size());
    }

    @Test
    public void testInsertEqualElementsIntoSortedSetWithoutComparator() {
        TreeSet<Integer> ti = new TreeSet<Integer>();
        ti.add(new Integer(1));
        ti.add(new Integer(2));
        assertEquals(2, ti.size());
    }
}
