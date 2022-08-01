package com.sap.sailing.server.trackfiles.test;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.common.scalablevalue.impl.ScalableDuration;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.test.AbstractLeaderboardTest;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.impl.DynamicGPSFixMovingTrackImpl;
import com.sap.sailing.server.trackfiles.RouteConverterGPSFixImporterFactory;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Util;
import com.sap.sse.util.kmeans.Cluster;
import com.sap.sse.util.kmeans.KMeansClusterer;

/**
 * See also bug 5728. We have seen tracks coming from phones where there seem to be two
 * "interleaved" sub-sequences of fixes: one where the time stamps are rounded to a full
 * second; and one where time stamps have a fractional seconds value. Each of these
 * sub-sequences seems to be consistent in itself, but at their boundaries the track
 * appears jittery and jumpy.<p>
 * 
 * We surmise that a constant offset can be computed by which the full-second time stamps
 * would need to be adjusted in order to result in a consistent track that is smooth also
 * at the boundaries between the two sub-sequences.<p>
 * 
 * This test starts with looking at two tracks that are known to show this issue. Jumpiness
 * can be measured by looking at the number and badness of inconsistencies between computed
 * and reported COG/SOG values between fixes, then "learning" the offset, adjusting all
 * integer-second fixes by the offset and computing number and badness of inconsistencies
 * again.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class JumpyTrackSmootheningTest {
    private static final Logger logger = Logger.getLogger(JumpyTrackSmootheningTest.class.getName());

    private DynamicGPSFixTrack<Competitor, GPSFixMoving> track;
    
    protected void readTrack(String filename) throws Exception {
        track = new DynamicGPSFixMovingTrackImpl<Competitor>(AbstractLeaderboardTest.createCompetitor(filename),
                /* millisecondsOverWhichToAverage */ 5000, /* losslessCompaction */ true);
        final InputStream fileInputStream = getClass().getClassLoader().getResourceAsStream(filename);
        final InputStream inputStream;
        if (filename.endsWith(".gz")) {
            inputStream = new GZIPInputStream(fileInputStream);
        } else {
            inputStream = fileInputStream;
        }
        RouteConverterGPSFixImporterFactory.INSTANCE.createRouteConverterGPSFixImporter().importFixes(inputStream,
                (fix, device)->track.add((GPSFixMoving) fix), /* inferSpeedAndBearing */ false, filename);
    }
    
    /**
     * On {@link #track} looks at adjacent fixes and compares the COG/SOG values reported by those fixes
     * with the COG/SOG value inferred from their position and time delta.
     */
    private int getInconsistenciesOnRawFixes() {
        int numberOfInconsistencies = 0;
        Duration offsetSum = Duration.NULL;
        final List<ScalableDuration> offsets = new LinkedList<>();
        GPSFixMoving previous = null;
        track.lockForRead();
        try {
            for (final GPSFixMoving fix : track.getRawFixes()) { // raw fixes with ascending reported time
                if (previous != null) {
                    final SpeedWithBearing inferred = previous.getSpeedAndBearingRequiredToReach(fix);
                    final SpeedWithBearing reportedByPrevious = previous.getSpeed();
                    final SpeedWithBearing reportedByFix = fix.getSpeed();
                    if (tooDifferent(inferred, reportedByPrevious) || tooDifferent(inferred, reportedByFix)) {
                        /*
                         * Hypothesis: we have two sequences folded/merged into one, where one is offset to the other by
                         * some more or less constant duration. Both describe the movement of the same object. If we can
                         * determine the offset and identify the fixes of the sub-sequence subject to offsetting then we
                         * can correct those fixes by adding the offset to their timestamp.
                         * 
                         * We start with a fix that doesn't seem to fit the sequence when extrapolating from the
                         * "previous" fix. We do not know which of the two has the correct time, but we can try moving
                         * the "fix" along the time axis, searching for an offset that would minimize the difference
                         * between the position interpolated between two adjacent positions and the actual position of
                         * the fix. The interpolated time point minus the fix's reported time stamp then is a candidate
                         * for an offset.
                         * 
                         * However, the "fix" may be followed immediately by another fix of the same incorrect time line.
                         * Then, it would seem as if leaving it where it is gives a good prediction of the next position
                         * in the track where in fact that next fix would also need to be moved back or forward in time
                         * by the same offset.
                         * 
                         * At each boundary between the two sub-sequences a mismatch between position / course extrapolated
                         * and observed/reported would be detected. If the fix in hand is one of the "correct" time line,
                         * trying to determine its offset may be tricky because we would try to match it against fixes
                         * in the incorrect sparse sub-sequence that may not have enough fixes for a reasonable interpolation.
                         * 
                         * With this in mind we would always have to look "both ways," trying to find out whether the left or
                         * the right fix is easier to move by an offset to fit in.
                         */
                        numberOfInconsistencies++;
                        final Duration offset = findOptimalOffset(fix, track);
                        offsetSum = offsetSum.plus(offset);
                        offsets.add(new ScalableDuration(offset));
                    }
                }
                previous = fix;
            }
        } finally {
            track.unlockAfterRead();
        }
        logger.info("Average offset found: "+offsetSum.divide(numberOfInconsistencies));
        KMeansClusterer<Double, Duration, ScalableDuration> kMeansCluster = new KMeansClusterer<>(2, offsets);
        final Set<Cluster<ScalableDuration, Double, Duration, ScalableDuration>> clusters = kMeansCluster.getClusters();
        for (final Cluster<ScalableDuration, Double, Duration, ScalableDuration> cluster : clusters) {
            logger.info("Cluster: "+cluster);
        }
        return numberOfInconsistencies;
    }
    
    private Duration findOptimalOffset(GPSFixMoving fix, DynamicGPSFixTrack<Competitor, GPSFixMoving> track) {
        final Duration samplingInterval = track.getAverageIntervalBetweenFixes();
        final Iterator<GPSFixMoving> ascendingIterator = track.getFixesIterator(fix.getTimePoint(), /* inclusive */ false);
        final Iterator<GPSFixMoving> descendingIterator = track.getFixesDescendingIterator(fix.getTimePoint(), /* inclusive */ false);
        final Position predictedNextPosition = fix.getSpeed().travelTo(fix.getPosition(), samplingInterval);
        Duration ascendingOffset = null;
        Duration descendingOffset = null;
        Distance ascendingMinimum = new MeterDistance(Double.MAX_VALUE);
        Distance descendingMinimum = new MeterDistance(Double.MAX_VALUE);
        GPSFixMoving previousAscendingFix = null;
        GPSFixMoving previousDescendingFix = null;
        while ((ascendingOffset == null && ascendingIterator.hasNext()) || (descendingOffset == null && descendingIterator.hasNext())) {
            if (ascendingIterator.hasNext()) {
                final GPSFixMoving ascendingFix = ascendingIterator.next();
                final Distance d = ascendingFix.getPosition().getDistance(predictedNextPosition);
                if (d.compareTo(ascendingMinimum) < 0) {
                    ascendingMinimum = d;
                } else { // we found a minimum after fix:
                    ascendingOffset = fix.getTimePoint().until(previousAscendingFix.getTimePoint().minus(samplingInterval));
                }
                previousAscendingFix = ascendingFix;
            }
            if (descendingIterator.hasNext()) {
                final GPSFixMoving descendingFix = descendingIterator.next();
                final Distance d = descendingFix.getPosition().getDistance(predictedNextPosition);
                if (d.compareTo(descendingMinimum) < 0) {
                    descendingMinimum = d;
                } else { // we found a minimum after fix:
                    descendingOffset = fix.getTimePoint().until(previousDescendingFix.getTimePoint().minus(samplingInterval));
                }
                previousDescendingFix = descendingFix;
            }
        }
        return ascendingMinimum.compareTo(descendingMinimum) < 0 && ascendingOffset != null ?
                ascendingOffset : descendingOffset;
    }

    private boolean tooDifferent(SpeedWithBearing inferred, SpeedWithBearing reported) {
        return
                // speed difference must be less than 20% of 
                (Math.abs((inferred.getKnots()-reported.getKnots()) / inferred.getKnots()) > 0.7 ||
                // course difference must be less than 10deg
                Math.abs(inferred.getBearing().getDifferenceTo(reported.getBearing()).getDegrees()) > 30);
    }

    @Test
    public void testCZE2471() throws Exception {
        readTrack("CZE2471.gpx.gz");
        track.lockForRead();
        try {
            assertFalse(Util.isEmpty(track.getRawFixes()));
        } finally {
            track.unlockAfterRead();
        }
        final int numberOfInconsistencies = getInconsistenciesOnRawFixes();
        assertTrue(numberOfInconsistencies > 0);
    }
    
    @Test
    public void testCZE2956() throws Exception {
        readTrack("CZE2956.gpx.gz");
        track.lockForRead();
        try {
            assertFalse(Util.isEmpty(track.getRawFixes()));
        } finally {
            track.unlockAfterRead();
        }
        final int numberOfInconsistencies = getInconsistenciesOnRawFixes();
        assertTrue(numberOfInconsistencies > 0);
    }
    
    /**
     * See https://my.sapsailing.com/gwt/RaceBoard.html?regattaName=Oak+cliff+DH+Distance+Race&raceName=R1&leaderboardName=Oak+cliff+DH+Distance+Race&leaderboardGroupId=a3902560-6bfa-43be-85e1-2b82a4963416&eventId=bf48a59d-f2af-47b6-a2f7-a5b78b22b9f2&mode=FULL_ANALYSIS
     * for the original race.
     */
    @Test
    public void testGallagherZelenka() throws Exception {
        readTrack("GallagherZelenka.gpx.gz");
        track.lockForRead();
        try {
            assertFalse(Util.isEmpty(track.getRawFixes()));
        } finally {
            track.unlockAfterRead();
        }
        final int numberOfInconsistencies = getInconsistenciesOnRawFixes();
        assertTrue(numberOfInconsistencies > 0);
    }
}
