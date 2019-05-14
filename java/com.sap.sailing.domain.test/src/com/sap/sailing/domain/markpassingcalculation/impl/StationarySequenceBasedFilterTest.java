package com.sap.sailing.domain.markpassingcalculation.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.TreeSet;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.common.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.markpassingcalculation.Candidate;
import com.sap.sailing.domain.test.TrackBasedTest;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.impl.DynamicGPSFixMovingTrackImpl;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Speed;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.DegreeBearingImpl;

public class StationarySequenceBasedFilterTest extends AbstractCandidateFilterTestSupport {
    private StationarySequence stationarySequence;
    private DynamicGPSFixTrack<Competitor, GPSFixMoving> track;
    private final static int FIXES_BETWEEN_CANDIDATES = 3;
    
    @Before
    public void setUp() {
        super.setUp();
        track = new DynamicGPSFixMovingTrackImpl<Competitor>(TrackBasedTest.createCompetitorWithBoat("Someone"), /* millisecondsOverWhichToAverage */ 15000);
        // construct a track such that all ten candidates at first end up within a single stationary sequence:
        Position position = new DegreePosition(0, 0);
        final SpeedWithBearing verySmallSpeed = new KnotSpeedWithBearingImpl(StationarySequence.CANDIDATE_FILTER_DISTANCE.scale(0.5/ /* number of candidates */ 10. / (double) FIXES_BETWEEN_CANDIDATES).
                inTime(c1.getTimePoint().until(c10.getTimePoint())).getKnots(), new DegreeBearingImpl(0));
        TimePoint previousTimePoint = c1.getTimePoint();
        for (final Candidate candidate : new Candidate[] { c2, c3, c4, c5, c6, c7, c8, c9, c10 }) {
            final Duration durationBetweenFixes = previousTimePoint.until(candidate.getTimePoint()).divide(FIXES_BETWEEN_CANDIDATES);
            TimePoint timePoint = previousTimePoint;
            for (int i=0; i<FIXES_BETWEEN_CANDIDATES; i++) {
                if (previousTimePoint != null) {
                    position = verySmallSpeed.travelTo(position, durationBetweenFixes);
                }
                track.add(new GPSFixMovingImpl(position, timePoint, verySmallSpeed));
                timePoint = timePoint.plus(durationBetweenFixes);
            }
            previousTimePoint = candidate.getTimePoint();
        }
        stationarySequence = new StationarySequence(c1, candidateComparator, track);
        TreeSet<Candidate> candidatesEffectivelyAdded = new TreeSet<>(candidateComparator);
        TreeSet<Candidate> candidatesEffectivelyRemoved = new TreeSet<>(candidateComparator);
        for (final Candidate candidate : new Candidate[] { c2, c3, c4, c5, c6, c7, c8, c9, c10 }) {
            assertTrue(stationarySequence.tryToExtendAfterLast(candidate, candidatesEffectivelyAdded, candidatesEffectivelyRemoved));
        }
    }
    
    @Test
    public void basicStationarySequenceTest() {
        assertEquals(10, stationarySequence.size());
        assertContainsExactly(stationarySequence.getAllCandidates(), c1, c2, c3, c4, c5, c6, c7, c8, c9, c10);
        final Iterable<Candidate> validCandidates = stationarySequence.getValidCandidates();
        assertContains(validCandidates, c1, c10); // at least the first and the last are expected to be contained
        // but those in the middle, particularly with the gap between c5 and c6, are not expected to be contained
        assertFalse(Util.contains(validCandidates, c5));
        assertFalse(Util.contains(validCandidates, c6));
    }

    @Test
    public void testAddingOutlierFix() {
        // additional set-up:
        final GPSFixMoving fixAtC2 = track.getFirstFixAtOrAfter(c2.getTimePoint());
        final GPSFixMoving fixAfterFixAtC2 = track.getFirstFixAfter(fixAtC2.getTimePoint());
        assertNotSame(fixAtC2, fixAfterFixAtC2);
        final Bearing outlierCourse = fixAtC2.getSpeed().getBearing().add(new DegreeBearingImpl(90));
        // add fix in between, traveling more than StationarySequence.CANDIDATE_FILTER_DISTANCE
        final Position outlierPosition = fixAtC2.getPosition().translateGreatCircle(
                outlierCourse, StationarySequence.CANDIDATE_FILTER_DISTANCE.scale(2));
        final TimePoint inBetweenTimePoint = fixAtC2.getTimePoint().plus(fixAtC2.getTimePoint().until(fixAfterFixAtC2.getTimePoint()).divide(2));
        final Speed outlierSpeed = fixAtC2.getPosition().getDistance(outlierPosition).inTime(fixAtC2.getTimePoint().until(inBetweenTimePoint));
        final GPSFixMoving fixInBetween = new GPSFixMovingImpl(outlierPosition, inBetweenTimePoint, new KnotSpeedWithBearingImpl(outlierSpeed.getKnots(), outlierCourse));
        track.add(fixInBetween);
        TreeSet<Candidate> candidatesEffectivelyAdded = new TreeSet<>(candidateComparator);
        TreeSet<Candidate> candidatesEffectivelyRemoved = new TreeSet<>(candidateComparator);
        final StationarySequence splitResult = stationarySequence.tryToAddFix(fixInBetween, candidatesEffectivelyAdded, candidatesEffectivelyRemoved,
                /* stationarySequenceSetToUpdate */ null, /* isReplacement */ false);
        // assertions:
        assertEquals(2, stationarySequence.size());
        assertContainsExactly(stationarySequence.getAllCandidates(), c1, c2);
        assertContainsExactly(stationarySequence.getValidCandidates(), c1, c2);
        assertContainsExactly(candidatesEffectivelyRemoved, c3, c4, c5, c6, c7, c8, c9, c10);
        assertTrue(candidatesEffectivelyAdded.isEmpty());
        assertEquals(8, splitResult.size());
        assertContainsExactly(splitResult.getAllCandidates(), c3, c4, c5, c6, c7, c8, c9, c10);
        assertContains(splitResult.getValidCandidates(), c3, c10);
    }
}
