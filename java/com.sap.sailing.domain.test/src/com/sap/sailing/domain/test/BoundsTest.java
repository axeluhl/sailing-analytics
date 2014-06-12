package com.sap.sailing.domain.test;

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
}
