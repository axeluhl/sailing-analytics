package com.sap.sailing.domain.common.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.common.impl.Util;

public class UtilTest {
    private List<Integer> l;
    
    @Before
    public void setUp() {
        l = new ArrayList<>();
        l.add(1);
        l.add(2);
        l.add(null);
        l.add(4);
    }

    @Test
    public void testContainsWithNull() {
        assertTrue(Util.contains(l, 1));
        assertFalse(Util.contains(l, 5));
        assertTrue(Util.contains(l, null));
    }
    
    @Test
    public void testContainsWithoutNull() {
        l.remove(null);
        assertFalse(Util.contains(l, null));
    }
    
    @Test
    public void testIndexOfWithNull() {
        assertEquals(0, Util.indexOf(l, 1));
        assertEquals(2, Util.indexOf(l, null));
    }
}
