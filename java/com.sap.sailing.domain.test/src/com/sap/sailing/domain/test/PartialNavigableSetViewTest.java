package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.TreeSet;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.tracking.impl.PartialNavigableSetView;

public class PartialNavigableSetViewTest {
    private PartialNavigableSetView<Integer> fullSet;
    private PartialNavigableSetView<Integer> emptySet;
    private TreeSet<Integer> set;
    
    @Before
    public void setUp() {
        set = new TreeSet<Integer>();
        fullSet = new PartialNavigableSetView<Integer>(set) {
            @Override
            protected boolean isValid(Integer e) {
                return true;
            }
        };
        emptySet = new PartialNavigableSetView<Integer>(set) {
            @Override
            protected boolean isValid(Integer e) {
                return false;
            }
        };
    }
    
    @Test
    public void testThatRejectingAllAlwaysYieldsEmptySet() {
        set.add(1);
        set.add(2);
        set.add(3);
        assertTrue(emptySet.isEmpty());
        set.add(4);
        assertTrue(emptySet.isEmpty());
    }

    @Test
    public void testThatRejectingAllAlwaysReturnsZeroSize() {
        set.add(1);
        set.add(2);
        set.add(3);
        assertEquals(0, emptySet.size());
        set.add(4);
        assertEquals(0, emptySet.size());
    }

    @Test
    public void testThatRejectingNoneNeverReturnsEmptySet() {
        set.add(1);
        set.add(2);
        set.add(3);
        assertFalse(fullSet.isEmpty());
        set.add(4);
        assertFalse(fullSet.isEmpty());
    }
    @Test
    public void testThatRejectingNoneAlwaysReturnsFullSize() {
        set.add(1);
        set.add(2);
        set.add(3);
        assertEquals(3, fullSet.size());
        set.add(4);
        assertEquals(4, fullSet.size());
    }
}
