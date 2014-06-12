package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.sap.sailing.domain.common.Bounds;
import com.sap.sailing.domain.common.impl.BoundsImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;

public class BoundsTest {
    @Test
    public void simpleContains() {
        Bounds b = new BoundsImpl(new DegreePosition(1, 1), new DegreePosition(3, 3));
        assertTrue(b.contains(new DegreePosition(2, 2)));
        assertFalse(b.contains(new DegreePosition(4, 4)));
        assertFalse(b.contains(new DegreePosition(0, 0)));
        assertFalse(b.contains(new DegreePosition(0, 2)));
        assertFalse(b.contains(new DegreePosition(2, 0)));
        assertFalse(b.contains(new DegreePosition(2, 4)));
    }
    
    @Test
    public void crossDateLineContains() {
        Bounds b = new BoundsImpl(new DegreePosition(1, 179), new DegreePosition(3, -179));
        assertTrue(b.contains(new DegreePosition(2, 180)));
        assertFalse(b.contains(new DegreePosition(4, -178)));
        assertFalse(b.contains(new DegreePosition(0, 178)));
        assertFalse(b.contains(new DegreePosition(0, 180)));
        assertFalse(b.contains(new DegreePosition(2, 178)));
        assertFalse(b.contains(new DegreePosition(2, -178)));
    }
    
    @Test
    public void simpleUnionTestOverlap() {
        Bounds b1 = new BoundsImpl(new DegreePosition(1, 1), new DegreePosition(3, 3));
        Bounds b2 = new BoundsImpl(new DegreePosition(2, 2), new DegreePosition(4, 4));
        Bounds u = b1.union(b2);
        assertEquals(1.0, u.getSouthWest().getLatDeg(), 0.00000001);
        assertEquals(1.0, u.getSouthWest().getLngDeg(), 0.00000001);
        assertEquals(4.0, u.getNorthEast().getLatDeg(), 0.00000001);
        assertEquals(4.0, u.getNorthEast().getLngDeg(), 0.00000001);
    }

    @Test
    public void testDatelineUnionOverlap() {
        Bounds b1 = new BoundsImpl(new DegreePosition(1, 179), new DegreePosition(3, -179));
        Bounds b2 = new BoundsImpl(new DegreePosition(2, 180), new DegreePosition(4, -178));
        Bounds u = b1.union(b2);
        assertEquals(1.0, u.getSouthWest().getLatDeg(), 0.00000001);
        assertEquals(179, u.getSouthWest().getLngDeg(), 0.00000001);
        assertEquals(4.0, u.getNorthEast().getLatDeg(), 0.00000001);
        assertEquals(-178, u.getNorthEast().getLngDeg(), 0.00000001);
    }

    @Test
    public void testDatelineUnionOutside() {
        Bounds b1 = new BoundsImpl(new DegreePosition(1, 179), new DegreePosition(3, -179));
        Bounds b2 = new BoundsImpl(new DegreePosition(4, -178), new DegreePosition(5, -177));
        Bounds u = b1.union(b2);
        assertEquals(1.0, u.getSouthWest().getLatDeg(), 0.00000001);
        assertEquals(179, u.getSouthWest().getLngDeg(), 0.00000001);
        assertEquals(5.0, u.getNorthEast().getLatDeg(), 0.00000001);
        assertEquals(-177, u.getNorthEast().getLngDeg(), 0.00000001);
    }

    @Test
    public void testDatelineUnionOneSideAndOtherSide() {
        Bounds b1 = new BoundsImpl(new DegreePosition(1, 178), new DegreePosition(3, 179));
        Bounds b2 = new BoundsImpl(new DegreePosition(4, -178), new DegreePosition(5, -177));
        Bounds u = b1.union(b2);
        assertEquals(1.0, u.getSouthWest().getLatDeg(), 0.00000001);
        assertEquals(178, u.getSouthWest().getLngDeg(), 0.00000001);
        assertEquals(5.0, u.getNorthEast().getLatDeg(), 0.00000001);
        assertEquals(-177, u.getNorthEast().getLngDeg(), 0.00000001);
    }

    @Test
    public void simpleUnionTestOutside() {
        Bounds b1 = new BoundsImpl(new DegreePosition(1, 1), new DegreePosition(3, 3));
        Bounds b2 = new BoundsImpl(new DegreePosition(4, 4), new DegreePosition(5, 5));
        Bounds u = b1.union(b2);
        assertEquals(1.0, u.getSouthWest().getLatDeg(), 0.00000001);
        assertEquals(1.0, u.getSouthWest().getLngDeg(), 0.00000001);
        assertEquals(5.0, u.getNorthEast().getLatDeg(), 0.00000001);
        assertEquals(5.0, u.getNorthEast().getLngDeg(), 0.00000001);
    }
    
    @Test
    public void simpleIntersectsTest() {
        Bounds b1 = new BoundsImpl(new DegreePosition(1, 1), new DegreePosition(3, 3));
        Bounds b2 = new BoundsImpl(new DegreePosition(2, 2), new DegreePosition(5, 5));
        assertTrue(b1.intersects(b2));
        assertTrue(b2.intersects(b1));
    }

    @Test
    public void simpleNegativeIntersectsTest() {
        Bounds b1 = new BoundsImpl(new DegreePosition(1, 1), new DegreePosition(3, 3));
        Bounds b2 = new BoundsImpl(new DegreePosition(4, 4), new DegreePosition(5, 5));
        assertFalse(b1.intersects(b2));
        assertFalse(b2.intersects(b1));
    }

    @Test
    public void simpleNegativeIntersectsTestNonIntersectingLat() {
        Bounds b1 = new BoundsImpl(new DegreePosition(1, 1), new DegreePosition(3, 3));
        Bounds b2 = new BoundsImpl(new DegreePosition(4, 2), new DegreePosition(5, 5));
        assertFalse(b1.intersects(b2));
        assertFalse(b2.intersects(b1));
    }

    @Test
    public void intersectsWithContainsTest() {
        Bounds b1 = new BoundsImpl(new DegreePosition(1, 1), new DegreePosition(4, 4));
        Bounds b2 = new BoundsImpl(new DegreePosition(2, 2), new DegreePosition(3, 3));
        assertTrue(b1.intersects(b2));
        assertTrue(b2.intersects(b1));
    }

    @Test
    public void dateLineIntersectsTest() {
        Bounds b1 = new BoundsImpl(new DegreePosition(1, 178), new DegreePosition(3, -177));
        Bounds b2 = new BoundsImpl(new DegreePosition(2, -178), new DegreePosition(5, -176));
        assertTrue(b1.intersects(b2));
        assertTrue(b2.intersects(b1));
    }

    @Test
    public void dateLineNegativeIntersectsTestNonMatchingLat() {
        Bounds b1 = new BoundsImpl(new DegreePosition(1, 178), new DegreePosition(3, -177));
        Bounds b2 = new BoundsImpl(new DegreePosition(4, -178), new DegreePosition(5, -176));
        assertFalse(b1.intersects(b2));
        assertFalse(b2.intersects(b1));
    }

    @Test
    public void dateLineNegativeIntersectsTest() {
        Bounds b1 = new BoundsImpl(new DegreePosition(1, 178), new DegreePosition(3, 179));
        Bounds b2 = new BoundsImpl(new DegreePosition(2, -178), new DegreePosition(5, -177));
        assertFalse(b1.intersects(b2));
        assertFalse(b2.intersects(b1));
    }

    @Test
    public void dateLineNegativeIntersectsTestBoundsCrossingDateLine() {
        Bounds b1 = new BoundsImpl(new DegreePosition(1, 178), new DegreePosition(3, -179));
        Bounds b2 = new BoundsImpl(new DegreePosition(2, -178), new DegreePosition(5, -177));
        assertFalse(b1.intersects(b2));
        assertFalse(b2.intersects(b1));
    }
}
