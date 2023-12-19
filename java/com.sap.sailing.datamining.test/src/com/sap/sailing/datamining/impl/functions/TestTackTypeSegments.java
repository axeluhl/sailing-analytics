//package com.sap.sailing.datamining.impl.functions;
//
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.UUID;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.mockito.ArgumentMatchers;
//import org.mockito.Mockito;
//
//import com.sap.sailing.domain.base.CourseArea;
//import com.sap.sailing.domain.base.Mark;
//import com.sap.sailing.domain.base.RaceDefinition;
//import com.sap.sailing.domain.base.Waypoint;
//import com.sap.sailing.domain.base.impl.CourseAreaImpl;
//import com.sap.sailing.domain.base.impl.CourseImpl;
//import com.sap.sailing.domain.base.impl.MarkImpl;
//import com.sap.sailing.domain.base.impl.WaypointImpl;
//import com.sap.sailing.domain.test.BravoFixTrackFoiledDistanceCacheTest.TimeRangeCacheWithParallelTestSupport;
//import com.sap.sailing.domain.tracking.DynamicBravoFixTrack;
//import com.sap.sailing.domain.tracking.MarkPositionAtTimePointCache;
//import com.sap.sailing.domain.tracking.TrackedLeg;
//import com.sap.sailing.domain.tracking.impl.BravoFixTrackImpl;
//import com.sap.sailing.domain.tracking.impl.DynamicGPSFixMovingTrackImpl;
//import com.sap.sailing.domain.tracking.impl.DynamicGPSFixTrackImpl;
//import com.sap.sailing.domain.tracking.impl.DynamicTrackedRaceImpl;
//import com.sap.sailing.domain.tracking.impl.TimeRangeCache;
//import com.sap.sailing.domain.tracking.impl.TrackedLegImpl;
//import com.sap.sse.common.TimePoint;
//
//
//public class TestTackTypeSegments {
//    private DynamicBravoFixTrack<CourseArea> track;
//    private DynamicGPSFixMovingTrackImpl<CourseArea> gpsTrack;
//    private TimeRangeCacheWithParallelTestSupport<CourseArea> foilingDistanceCache;
//    
//    @Before
//    public void setup () {
//    //mockito sache
//    final IControl cp1 = Mockito.mock(IControl.class);
//    Mockito.when(cp1.getName()).thenReturn(name);
//    Mockito.when(cp1.getSize()).thenReturn(numberOfMarks);
//    Mockito.when(cp1.getId()).thenReturn(id);
//
//    
//    //foiling Sachen
//    final CourseAreaImpl courseArea = new CourseAreaImpl("Test", UUID.randomUUID(), /* centerPosition */ null, /* radius */ null);
//    gpsTrack = new DynamicGPSFixMovingTrackImpl<>(courseArea, /* millisecondsOverWhichToAverage */ 15000);
//    track = new BravoFixTrackImpl<CourseArea>(courseArea, "test", /* hasExtendedFixes */ true, gpsTrack) {
//        private static final long serialVersionUID = 1473560197177750211L;
//
//        @Override
//        protected <T> TimeRangeCache<T> createTimeRangeCache(CourseArea trackedItem, final String cacheName) {
//            return new TimeRangeCacheWithParallelTestSupport<>(cacheName);
//        }
//    };
//    track.add(createFix(1000l, /* rideHeightPort */ 0.6, /* rideHeightStarboard */ 0.6, /* heel */ 10., /* pitch */ 5.));
//    track.add(createFix(2000l, /* rideHeightPort */ 0.6, /* rideHeightStarboard */ 0.6, /* heel */ 10., /* pitch */ 5.));
//    track.add(createFix(3000l, /* rideHeightPort */ 0.6, /* rideHeightStarboard */ 0.6, /* heel */ 10., /* pitch */ 5.));
//    gpsTrack.add(createGPSFix(1000l, 0, 0, 0, 1));
//    gpsTrack.add(createGPSFix(2000l, 1./3600./60., 0, 0, 1));
//    gpsTrack.add(createGPSFix(3000l, 2./3600./60., 0, 0, 1));
//    foilingDistanceCache = TimeRangeCacheWithParallelTestSupport.getCacheByName("foilingDistanceCache");
//    
//    DynamicTrackedRaceImpl trackedRace = mock(DynamicTrackedRaceImpl.class);
//    when(trackedRace.getCenterOfCourse(ArgumentMatchers.any(TimePoint.class))).thenCallRealMethod();
//    when(trackedRace.getApproximatePosition(ArgumentMatchers.any(Waypoint.class), ArgumentMatchers.any(TimePoint.class))).thenCallRealMethod();
//    when(trackedRace.getApproximatePosition(ArgumentMatchers.any(Waypoint.class), ArgumentMatchers.any(TimePoint.class), ArgumentMatchers.any(MarkPositionAtTimePointCache.class))).thenCallRealMethod();
//    RaceDefinition race = mock(RaceDefinition.class);
//    when(trackedRace.getRace()).thenReturn(race);
//    mark1 = new MarkImpl("1");
//    mark2 = new MarkImpl("2");
//    wp1 = new WaypointImpl(mark1);
//    wp2 = new WaypointImpl(mark2);
//    course = new CourseImpl("The Course", Arrays.asList(wp1, wp2));
//    when(race.getCourse()).thenReturn(course);
//    mark1Track = new DynamicGPSFixTrackImpl<Mark>(mark1, /* millisecondsOverWhichToAverage */ 10);
//    mark2Track = new DynamicGPSFixTrackImpl<Mark>(mark1, /* millisecondsOverWhichToAverage */ 10);
//    when(trackedRace.getOrCreateTrack(mark1)).thenReturn(mark1Track);
//    when(trackedRace.getOrCreateTrack(mark2)).thenReturn(mark2Track);
//    final TrackedLeg trackedLeg = new TrackedLegImpl(trackedRace, course.getLeg(0), Collections.emptySet());
//    when(trackedRace.getTrackedLeg(course.getLeg(0))).thenReturn(trackedLeg);
//}
//    
//    @Test
//    public void testingMissingMarkPassing () {}
//    //missing + skipped
//   
//    @Test
//    public void testingFixExactOnMarkPassing () {}
//   
//    @Test
//    public void testingOpenEndedRace () {}
//   
//    @Test
//    public void testingFinishedRace () {}
//    
//    
//}
