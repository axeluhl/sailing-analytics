package com.sap.sailing.domain.tracking.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.NavigableSet;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sse.common.TimePoint;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestVirtualWindFixTrackTimepointRounding {
    private static final int RESOLUTION_IN_MILLIS = 1000;
    private VirtualWindFixesAsNavigableSet virtualWindFixes;
    
    @Before
    public void setUp() {
        final TrackedRace trackedRace = mock(TrackedRace.class);
        when(trackedRace.getTimePointOfNewestEvent()).thenReturn(TimePoint.of(0));
        virtualWindFixes = new VirtualWindFixesAsNavigableSet(null, trackedRace, RESOLUTION_IN_MILLIS) {
            private static final long serialVersionUID = 1L;

            @Override
            protected Wind getWind(Position p, TimePoint timePoint) {
                return null;
            }

            @Override
            protected NavigableSet<Wind> createSubset(WindTrack track, TrackedRace trackedRace, TimePoint from,
                    TimePoint to) {
                return null;
            }
        };
    }
    
    @Test
    public void testLoweringBeginningOfEpoch() {
        final TimePoint epoch = TimePoint.of(0);
        final TimePoint lower = virtualWindFixes.lowerToResolution(epoch);
        assertNotEquals(epoch, lower);
        assertTrue(epoch.compareTo(lower) > 0);
    }

    @Test
    public void testLoweringBeforefEpoch() {
        final TimePoint beforeEpoch = TimePoint.of(-10);
        final TimePoint lower = virtualWindFixes.lowerToResolution(beforeEpoch);
        assertNotEquals(beforeEpoch, lower);
        assertTrue(beforeEpoch.compareTo(lower) > 0);
    }

    @Test
    public void testCeilingBeforeEpoch() {
        final TimePoint beforeEpoch = TimePoint.of(-RESOLUTION_IN_MILLIS-1);
        final TimePoint higher = virtualWindFixes.ceilingToResolution(beforeEpoch);
        assertEquals(TimePoint.of(-RESOLUTION_IN_MILLIS), higher);
        assertTrue(beforeEpoch.compareTo(higher) < 0);
    }

    @Test
    public void testHigherBeginningOfEpoch() {
        final TimePoint epoch = TimePoint.of(0);
        final TimePoint higher = virtualWindFixes.higherToResolution(epoch);
        assertNotEquals(epoch, higher);
        assertTrue(epoch.compareTo(higher) < 0);
    }

    @Test
    public void testHigherBeforeEpoch() {
        final TimePoint beforeEpoch = TimePoint.of(-RESOLUTION_IN_MILLIS-1);
        final TimePoint higher = virtualWindFixes.higherToResolution(beforeEpoch);
        assertNotEquals(beforeEpoch, higher);
        assertTrue(beforeEpoch.compareTo(higher) < 0);
        assertEquals(TimePoint.of(-RESOLUTION_IN_MILLIS), higher);
    }
}
