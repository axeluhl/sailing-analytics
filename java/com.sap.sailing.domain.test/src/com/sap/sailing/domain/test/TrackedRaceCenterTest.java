package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.MarkImpl;
import com.sap.sailing.domain.base.impl.WaypointImpl;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.impl.GPSFixImpl;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.MarkPositionAtTimePointCache;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.impl.DynamicGPSFixTrackImpl;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRaceImpl;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * Tests the calculation of the center of the course, particularly the
 * {@link TrackedRace#getCenterOfCourse(com.sap.sse.common.TimePoint)} method.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public class TrackedRaceCenterTest {
    private Mark mark1;
    private Mark mark2;
    private Waypoint wp1;
    private Waypoint wp2;
    private Course course;
    private DynamicGPSFixTrack<Mark, GPSFix> mark1Track;
    private DynamicGPSFixTrack<Mark, GPSFix> mark2Track;
    private DynamicTrackedRaceImpl trackedRace;

    @Before
    public void setUp() {
        trackedRace = mock(DynamicTrackedRaceImpl.class);
        when(trackedRace.getCenterOfCourse(Matchers.any(TimePoint.class))).thenCallRealMethod();
        when(trackedRace.getApproximatePosition(Matchers.any(Waypoint.class), Matchers.any(TimePoint.class))).thenCallRealMethod();
        when(trackedRace.getApproximatePosition(Matchers.any(Waypoint.class), Matchers.any(TimePoint.class), Matchers.any(MarkPositionAtTimePointCache.class))).thenCallRealMethod();
        RaceDefinition race = mock(RaceDefinition.class);
        when(trackedRace.getRace()).thenReturn(race);
        course = mock(Course.class);
        when(race.getCourse()).thenReturn(course);
        mark1 = new MarkImpl("1");
        mark2 = new MarkImpl("2");
        wp1 = new WaypointImpl(mark1);
        wp2 = new WaypointImpl(mark2);
        when(course.getWaypoints()).thenReturn(Arrays.asList(new Waypoint[] { wp1, wp2 }));
        mark1Track = new DynamicGPSFixTrackImpl<Mark>(mark1, /* millisecondsOverWhichToAverage */ 10);
        mark2Track = new DynamicGPSFixTrackImpl<Mark>(mark1, /* millisecondsOverWhichToAverage */ 10);
        when(trackedRace.getOrCreateTrack(mark1)).thenReturn(mark1Track);
        when(trackedRace.getOrCreateTrack(mark2)).thenReturn(mark2Track);
    }
    
    @Test
    public void testSimpleAverage() {
        TimePoint now = MillisecondsTimePoint.now();
        Position mark1Pos = new DegreePosition(10, 10);
        mark1Track.add(new GPSFixImpl(mark1Pos, now));
        Position mark2Pos = new DegreePosition(20, 10);
        mark2Track.add(new GPSFixImpl(mark2Pos, now));
        Position center = trackedRace.getCenterOfCourse(now);
        assertEquals(15, center.getLatDeg(), 0.00001);
        assertEquals(10, center.getLngDeg(), 0.00001);
    }

    @Test
    public void testTriangle() {
        Mark mark3 = new MarkImpl("3");
        Waypoint wp3 = new WaypointImpl(mark3);
        when(course.getWaypoints()).thenReturn(Arrays.asList(new Waypoint[] { wp1, wp2, wp3 }));
        DynamicGPSFixTrackImpl<Mark> mark3Track = new DynamicGPSFixTrackImpl<Mark>(mark3, /* millisecondsOverWhichToAverage */ 10);
        when(trackedRace.getOrCreateTrack(mark3)).thenReturn(mark3Track);

        TimePoint now = MillisecondsTimePoint.now();
        Position mark1Pos = new DegreePosition(0, 0);
        mark1Track.add(new GPSFixImpl(mark1Pos, now));
        Position mark2Pos = new DegreePosition(0, 10);
        mark2Track.add(new GPSFixImpl(mark2Pos, now));
        Position mark3Pos = new DegreePosition(10, 5);
        mark3Track.add(new GPSFixImpl(mark3Pos, now));
        Position center = trackedRace.getCenterOfCourse(now);
        assertTrue(2 < center.getLatDeg() && center.getLatDeg() < 8);
        assertEquals(5, center.getLngDeg(), 0.00001);
    }
}
