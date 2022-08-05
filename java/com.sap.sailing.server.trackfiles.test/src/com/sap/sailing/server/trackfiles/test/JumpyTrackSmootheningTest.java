package com.sap.sailing.server.trackfiles.test;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
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
import com.sap.sse.common.TimePoint;
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
    
    private boolean isBoundaryBetweenCorrectAndIncorrectSubsequence(GPSFixMoving previous, GPSFixMoving fix) {
        final SpeedWithBearing inferred = previous.getSpeedAndBearingRequiredToReach(fix);
        final SpeedWithBearing reportedByPrevious = previous.getSpeed();
        final SpeedWithBearing reportedByFix = fix.getSpeed();
        // TODO for high frequency (~1Hz) tracks with approximately equal COG/SOG values for previous/fix as could additionally assume that fix needs to be close to the position extrapolated from previous and the average COG/SOG
        return tooDifferent(inferred, reportedByPrevious) || tooDifferent(inferred, reportedByFix);
    }
    
    /**
     * On {@link #track} looks at adjacent fixes and compares the COG/SOG values reported by those fixes
     * with the COG/SOG value inferred from their position and time delta.
     */
    private int getInconsistenciesOnRawFixes() {
        int numberOfInconsistencies = 0;
        Duration offsetSum = Duration.NULL;
        final Map<GPSFixMoving, ScalableDuration> offsets = new LinkedHashMap<>();
        final Map<GPSFixMoving, SpeedWithBearing> inferredSpeeds = new LinkedHashMap<>();
        GPSFixMoving previous = null;
        track.lockForRead();
        try {
            for (final GPSFixMoving fix : track.getRawFixes()) { // raw fixes with ascending reported time
                if (previous != null) {
                    inferredSpeeds.put(fix, previous.getSpeedAndBearingRequiredToReach(fix));
                    if (isBoundaryBetweenCorrectAndIncorrectSubsequence(previous, fix)) {
                        /*
                         * Hypothesis: we have a fix sequence that describes the trajectory of a sailing boat where some
                         * of the fixes have an incorrect time point. The offset of these incorrect time points varies.
                         * In the particular case observed, all regular fixes have a time point that is at a full second
                         * (UTC) with zero milliseconds, whereas all outliers have a non-zero millisecond part that does
                         * not fit the otherwise very regular sampling rate.
                         * 
                         * Due to the irregularity of the offsets there is no point in trying to "learn" this offset.
                         * Instead, it's more about recognizing the outliers which so far always seem to come as a
                         * single fix in a longer series of regular fixes, and then finding a good time point adjustment
                         * so it matches the sequence.
                         * 
                         * For identification, we use multiple hints:
                         *
                         * - a non-zero millisecond time point
                         * 
                         * - a noticeable mismatch either in SOG (in case the fix has a time stamp too early and
                         * actually was recorded later, so SOG is reported higher) with mostly consistent COG, or an
                         * approximately reverse COG (in case the fix was actually recorded earlier) with a more or less
                         * random SOG
                         *
                         * - the time point representing an inconsistency in an otherwise very regular sampling rate
                         * 
                         * - the fix position being very close to the remaining trajectory, such that a segment between
                         * two non-outlier fixes can be found to which the incorrectly-timed fix has a very small distance
                         * 
                         * With this in mind we would always have to look "both ways," trying to find out whether the fix
                         * originally had an earlier or a later time point that would bring it closely in line with the
                         * other fixes. The fix does contain valuable information despite its incorrect time point because
                         * it could indicate a deviation from the straight line otherwise connecting the two adjacent fixes.
                         * 
                         * To approximate the correct time point we look for the track segment closest to the fix's position,
                         * then project the fix onto it and split the segment's duration proportionately.
                         */
                        // FIXME: the problem is that fix or previous can be an outlier; if "previous" is the outlier, looking to move "fix" around won't help
                        numberOfInconsistencies++;
                        final Duration offset = findOptimalOffset(previous, fix, track);
                        offsetSum = offsetSum.plus(offset);
                        offsets.put(fix, new ScalableDuration(offset));
                    }
                }
                previous = fix;
            }
        } finally {
            track.unlockAfterRead();
        }
        logger.info("Average offset found: "+offsetSum.divide(numberOfInconsistencies));
        KMeansClusterer<Double, Duration, ScalableDuration> kMeansCluster = new KMeansClusterer<>(2, offsets.values());
        final Set<Cluster<ScalableDuration, Double, Duration, ScalableDuration>> clusters = kMeansCluster.getClusters();
        for (final Cluster<ScalableDuration, Double, Duration, ScalableDuration> cluster : clusters) {
            logger.info("Cluster: "+cluster);
        }
        return numberOfInconsistencies;
    }
    
    private Duration findOptimalOffset(GPSFixMoving previous, GPSFixMoving fix, DynamicGPSFixTrack<Competitor, GPSFixMoving> track) {
        final Iterator<GPSFixMoving> ascendingIterator = track.getFixesIterator(fix.getTimePoint(), /* inclusive */ true);
        final Duration ascendingOffset = findOptimalOffset(previous, ascendingIterator);
        final Iterator<GPSFixMoving> descendingIterator = track.getFixesDescendingIterator(previous.getTimePoint(), /* inclusive */ true);
        final Duration descendingOffset = findOptimalOffset(fix, descendingIterator);
        // Use the greater of the two offsets; the lesser will link it to its own sub-sequence neighbor
        return ascendingOffset != null && ascendingOffset.abs().compareTo(descendingOffset.abs()) > 0 ?
                ascendingOffset : descendingOffset;
    }
    
    /**
     * Starting with the first pair of fixes returned by the {@code iterator} looks for the minimal distance of fix's position
     * to the line connecting the pair of fixes.<p>
     * 
     * Should {@code fix} be consistent with the fixes from {@code iterator} then
     * the minimum distance is expected to be found right for the first pair of fixes, and that distance would then be the
     * typical distance traveled between to fixes at the COG/SOG reported. The offset computed should then be pretty close
     * to zero.<p>
     * 
     * Otherwise, a minimum would be found some number of fixes away. The distance of {@code fix}'s position two the two
     * other fixes will then be determined, and the duration between those fixes will be split proportionately based on the
     * respective distances of {@code fix}'s position to each of them to obtain a good estimate of its actual time point.
     * The difference between this inferred time point and the time point that {@code fix} reports is then used as the
     * offset.
     */
    private Duration findOptimalOffset(final GPSFixMoving fix, final Iterator<GPSFixMoving> iterator) {
        final Position fixPosition = fix.getPosition();
        GPSFixMoving lastFix = null;
        Distance minimum = new MeterDistance(Double.MAX_VALUE);
        Duration offset = null;
        boolean foundMinimum = false;
        while (!foundMinimum && iterator.hasNext()) {
            final GPSFixMoving currentFix = iterator.next();
            if (lastFix != null) {
                final Distance d = fixPosition.getDistanceToLine(lastFix.getPosition(), currentFix.getPosition()).abs();
                if (d.compareTo(minimum) < 0) {
                    minimum = d;
                    final Distance alongTrackDistanceFromLastFix = fixPosition.alongTrackDistance(lastFix.getPosition(), lastFix.getPosition().getBearingGreatCircle(currentFix.getPosition()));
                    // interpolate the time between the adjacent fixes to whose connection "fix" is closest, splitting the duration
                    // between the adjacent fixes proportionately based on "fix"'s distances to each of the two adjacent fixes:
                    final TimePoint inferredTimePointForFix = lastFix.getTimePoint().plus(lastFix.getTimePoint().until(currentFix.getTimePoint()).times(
                            alongTrackDistanceFromLastFix.divide(lastFix.getPosition().getDistance(currentFix.getPosition()))));
                    offset = fix.getTimePoint().until(inferredTimePointForFix);
                } else { // we found a minimum after fix:
                    foundMinimum = true;
                }
            }
            lastFix = currentFix;
        }
        return offset;
    }

    private boolean tooDifferent(SpeedWithBearing inferred, SpeedWithBearing reported) {
        final double speedRatio = Math.abs((inferred.getKnots()-reported.getKnots()) / reported.getKnots());
        return
                // speed difference must be less than 20% of 
                speedRatio > 2.0 ||
                // course difference must be less than 10deg
                Math.abs(inferred.getBearing().getDifferenceTo(reported.getBearing()).getDegrees()) > 120;
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
