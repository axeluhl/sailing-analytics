package com.sap.sailing.domain.test;

import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.impl.MarkImpl;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.impl.DynamicGPSFixTrackImpl;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRaceImpl;

public class MarkPositionTimeFilterTest {
    private DynamicTrackedRace trackedRace;
    private MarkImpl m;
    private DynamicGPSFixTrackImpl<Mark> track;
    
    @Before
    public void setUp() {
        trackedRace = mock(DynamicTrackedRaceImpl.class);
        m = new MarkImpl("Test Mark");
        track = new DynamicGPSFixTrackImpl<Mark>(m, /* millisecondsOverWhichToAverage */ 5000);
        when(trackedRace.getOrCreateTrack(m)).thenReturn(track);
        doCallRealMethod().when(trackedRace).recordFix(same(m), (GPSFixMoving) anyObject());
    }
    
    @Test
    public void generalSetupTest() {
        assertSame(track, trackedRace.getOrCreateTrack(m));
    }
}
