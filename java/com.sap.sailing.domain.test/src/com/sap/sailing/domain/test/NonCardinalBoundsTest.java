package com.sap.sailing.domain.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.sap.sailing.domain.common.NonCardinalBounds;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.NauticalMileDistance;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.impl.DegreeBearingImpl;

public class NonCardinalBoundsTest {
    @Test
    public void testSpecialCaseCardinalBounds() {
        final NonCardinalBounds bounds = NonCardinalBounds.create(
                new DegreePosition(0, 0),
                Bearing.NORTH,
                /* verticalSize */ new NauticalMileDistance(1),
                /* horizontalSize */ new NauticalMileDistance(1));
        final Position leftOutside = new DegreePosition(1./120., -1./120.);
        final Position topOutside = new DegreePosition(1./30., 1./120.);
        final Position rightOutside = new DegreePosition(1./120., 1./30.);
        final Position bottomOutside = new DegreePosition(-1./120., 1./120.);
        assertFalse(bounds.contains(leftOutside));
        assertFalse(bounds.contains(topOutside));
        assertFalse(bounds.contains(rightOutside));
        assertFalse(bounds.contains(bottomOutside));
        assertTrue(bounds.contains(new DegreePosition(1./120., 1./120.)));
    }
    
    @Test
    public void testExtendByPosition() {
        final NonCardinalBounds bounds = NonCardinalBounds.create(
                new DegreePosition(49, 8),
                new DegreeBearingImpl(30),
                /* verticalSize */ new NauticalMileDistance(1),
                /* horizontalSize */ new NauticalMileDistance(1));
        final Position leftOutside = new DegreePosition(49.+1./120., 8); // left of the 30deg inclined left border
        assertFalse(bounds.contains(leftOutside));
        final NonCardinalBounds extendedBounds = bounds.extend(leftOutside);
        assertTrue(extendedBounds.contains(leftOutside));
    }
}
