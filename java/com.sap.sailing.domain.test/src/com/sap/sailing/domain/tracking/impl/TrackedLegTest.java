package com.sap.sailing.domain.tracking.impl;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.WaypointImpl;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class TrackedLegTest {
    
    private TrackedLegImpl trackedLegImpl;
    private DynamicTrackedRaceImpl dynamicTrackedRace;
    private Leg leg;
    
    @Before
    public void setUp() {
        leg = Mockito.mock(Leg.class);
        dynamicTrackedRace = Mockito.mock(DynamicTrackedRaceImpl.class);
        trackedLegImpl = new TrackedLegImpl(dynamicTrackedRace, leg, Collections.<Competitor>emptyList());
    }
    
    @Test
    public void testEquidistantSectionsOfLeg() {
        TimePoint at = new MillisecondsTimePoint(1000);
        Waypoint start = new WaypointImpl(DomainFactory.INSTANCE.getOrCreateMark("mark"));
        Waypoint finish = new WaypointImpl(DomainFactory.INSTANCE.getOrCreateMark("mark2"));
        Mockito.when(leg.getFrom()).thenReturn(start);
        Mockito.when(leg.getTo()).thenReturn(finish);
        Mockito.when(dynamicTrackedRace.getApproximatePosition(start, at)).thenReturn(new DegreePosition(1, 2));
        Mockito.when(dynamicTrackedRace.getApproximatePosition(finish, at)).thenReturn(new DegreePosition(2, 5));
        Iterable<Position> positions10 = trackedLegImpl.getEquidistantSectionsOfLeg(at, 10);
        assertEquals(11, Util.stream(positions10).count());
        Iterable<Position> positions15 = trackedLegImpl.getEquidistantSectionsOfLeg(at, 15);
        assertEquals(16, Util.stream(positions15).count());
        Iterable<Position> positions100 = trackedLegImpl.getEquidistantSectionsOfLeg(at, 100);
        assertEquals(101, Util.stream(positions100).count());
        
    }

}
