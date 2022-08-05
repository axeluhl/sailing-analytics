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
import com.sap.sailing.domain.common.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.test.AbstractLeaderboardTest;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.impl.DynamicGPSFixMovingTrackImpl;
import com.sap.sailing.server.trackfiles.RouteConverterGPSFixImporterFactory;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
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
     * For outlier identification, we use multiple hints:
     * <ul>
     * <li>a non-zero millisecond time point</li>
     * 
     * <li>the time point representing an inconsistency in an otherwise very regular sampling rate</li>
     * 
     * <li>a noticeable mismatch either in SOG (in case the fix has a time stamp too early and actually was recorded
     * later, so SOG is reported higher) with mostly consistent COG, or an approximately reverse COG (in case the fix
     * was actually recorded earlier) with a more or less random SOG</li>
     *
     * <li>the fix position being very close to the remaining trajectory, such that a segment between two non-outlier
     * fixes can be found to which the incorrectly-timed fix has a very small distance</li>
     * </ul>
     * 
     * @return {@code null} if at less than three of these four criteria are fulfilled for the {@code fix}; otherwise
     *         the adjusted fix with the new time point and the ratio between its distance from the closest track
     *         segment and that segment's length
     */
    private Pair<GPSFixMoving, Double> isLikelyOutlierWithCorrectableTimepoint(DynamicGPSFixTrack<Competitor, GPSFixMoving> track,
            GPSFixMoving previous, GPSFixMoving fix, GPSFixMoving next) {
        final int HOW_MANY_CRITERIA_TO_FULFILL = 3;
        final double DISTANCE_RATIO_TOLERANCE = 0.5; // ratio between cross-track distance and length of closest segment
        final Pair<GPSFixMoving, Double> adjustedFixAndDistance;
        int criteriaFulfilled = 0;
        if (hasNonZeroMilliseconds(fix.getTimePoint())) {
            criteriaFulfilled++;
        }
        if (isInconsistentWithSamplingRate(track, previous, fix, next)) {
            criteriaFulfilled++;
        }
        if (hasInconsistentCogSog(previous, fix, next)) {
            criteriaFulfilled++;
        }
        if (criteriaFulfilled >= HOW_MANY_CRITERIA_TO_FULFILL-1) {
            final Pair<GPSFixMoving, Double> adjusted = adjust(previous, fix, track);
            if (adjusted.getB() > DISTANCE_RATIO_TOLERANCE) {
                adjustedFixAndDistance = null;
            } else {
                adjustedFixAndDistance = adjusted;
                criteriaFulfilled++;
            }
        } else {
            adjustedFixAndDistance = null;
        }
        assert criteriaFulfilled >= 3 || adjustedFixAndDistance == null;
        return adjustedFixAndDistance;
    }
    
    private boolean hasInconsistentCogSog(GPSFixMoving previous, GPSFixMoving fix, GPSFixMoving next) {
        final SpeedWithBearing inferredBetweenPreviousAndFix = previous.getSpeedAndBearingRequiredToReach(fix);
        final SpeedWithBearing inferredBetweenFixAndNext = fix.getSpeedAndBearingRequiredToReach(next);
        final SpeedWithBearing reportedByPrevious = previous.getSpeed();
        final SpeedWithBearing reportedByFix = fix.getSpeed();
        final SpeedWithBearing reportedByNext = next.getSpeed();
        return isConsistent(reportedByPrevious, reportedByNext)
                && !isConsistent(reportedByPrevious, inferredBetweenPreviousAndFix)
                && !isConsistent(inferredBetweenFixAndNext, reportedByFix)
                && !isConsistent(inferredBetweenFixAndNext, reportedByNext);
    }
    
    private boolean isConsistent(double ratio, double tolerance) {
        return ratio < 1+tolerance && ratio > 1-tolerance; 
    }

    private boolean isConsistent(SpeedWithBearing a, SpeedWithBearing b) {
        final double SPEED_RATIO_TOLERANCE = 0.1;
        final double COURSE_DEGREE_TOLERANCE = 10;
        return isConsistent(a.getKnots()/b.getKnots(), SPEED_RATIO_TOLERANCE) &&
               a.getBearing().getDifferenceTo(b.getBearing()).abs().getDegrees() < COURSE_DEGREE_TOLERANCE;
    }

    private boolean isInconsistentWithSamplingRate(DynamicGPSFixTrack<Competitor, GPSFixMoving> track,
            GPSFixMoving previous, GPSFixMoving fix, GPSFixMoving next) {
        final double RATIO_TOLERANCE = 0.05;
        final Duration averageIntervalBetweenFixes = track.getAverageIntervalBetweenFixes();
        final double ratioPreviousToFix = previous.getTimePoint().until(fix.getTimePoint()).divide(averageIntervalBetweenFixes);
        final double ratioFixToNext = fix.getTimePoint().until(next.getTimePoint()).divide(averageIntervalBetweenFixes);
        return !isConsistent(ratioPreviousToFix, RATIO_TOLERANCE) || !isConsistent(ratioFixToNext, RATIO_TOLERANCE);
    }

    private boolean hasNonZeroMilliseconds(TimePoint timePoint) {
        return timePoint.asMillis() % 1000 != 0;
    }

    /**
     * On {@link #track} looks at adjacent fixes and compares the COG/SOG values reported by those fixes
     * with the COG/SOG value inferred from their position and time delta.
     */
    private int getInconsistenciesOnRawFixes() {
        int numberOfInconsistencies = 0;
        final DynamicGPSFixMovingTrackImpl<Competitor> replacedTrack = new DynamicGPSFixMovingTrackImpl<Competitor>(track.getTrackedItem(),
                /* millisecondsOverWhichToAverage */ 5000, /* losslessCompaction */ true);
        replacedTrack.suspendValidityCaching();
        Duration offsetSum = Duration.NULL;
        final Map<GPSFixMoving, ScalableDuration> offsets = new LinkedHashMap<>();
        final Map<GPSFixMoving, SpeedWithBearing> inferredSpeeds = new LinkedHashMap<>();
        GPSFixMoving previous = null, fix = null;
        track.lockForRead();
        try {
            for (final GPSFixMoving next : track.getRawFixes()) { // raw fixes with ascending reported time
                if (previous != null && fix != null) {
                    inferredSpeeds.put(fix, previous.getSpeedAndBearingRequiredToReach(fix));
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
                     * With this in mind we would always have to look "both ways," trying to find out whether the fix
                     * originally had an earlier or a later time point that would bring it closely in line with the
                     * other fixes. The fix does contain valuable information despite its incorrect time point because
                     * it could indicate a deviation from the straight line otherwise connecting the two adjacent fixes.
                     * 
                     * To approximate the correct time point we look for the track segment closest to the fix's position,
                     * then project the fix onto it and split the segment's duration proportionately.
                     */
                    final Pair<GPSFixMoving, Double> adjusted = isLikelyOutlierWithCorrectableTimepoint(track, previous, fix, next);
                    if (adjusted != null) {
                        numberOfInconsistencies++;
                        final GPSFixMoving replacementFix = adjusted.getA();
                        replacedTrack.add(replacementFix);
                    } else {
                        replacedTrack.add(fix);
                    }
                }
                previous = fix;
                fix = next;
            }
        } finally {
            track.unlockAfterRead();
        }
        final Map<GPSFixMoving, SpeedWithBearing> inferredSpeedsOnReplacedTrack = new LinkedHashMap<>();
        replacedTrack.lockForRead();
        try {
            GPSFixMoving previousReplaced = null;
            for (final GPSFixMoving fixReplaced : replacedTrack.getRawFixes()) {
                if (previousReplaced != null) {
                    inferredSpeedsOnReplacedTrack.put(fixReplaced, previousReplaced.getSpeedAndBearingRequiredToReach(fixReplaced));
                }
                previousReplaced = fixReplaced;
            }
        } finally {
            replacedTrack.unlockAfterRead();
        }
        logger.info("Average offset found: "+offsetSum.divide(numberOfInconsistencies));
        KMeansClusterer<Double, Duration, ScalableDuration> kMeansCluster = new KMeansClusterer<>(2, offsets.values());
        final Set<Cluster<ScalableDuration, Double, Duration, ScalableDuration>> clusters = kMeansCluster.getClusters();
        for (final Cluster<ScalableDuration, Double, Duration, ScalableDuration> cluster : clusters) {
            logger.info("Cluster: "+cluster);
        }
        return numberOfInconsistencies;
    }
    
    /**
     * @return the adjusted fix, and the ratio between the fix's cross-track distance from the nearest track segment and
     *         that segment's length
     */
    private Pair<GPSFixMoving, Double> adjust(GPSFixMoving previous, GPSFixMoving fix, DynamicGPSFixTrack<Competitor, GPSFixMoving> track) {
        final Iterator<GPSFixMoving> ascendingIterator = track.getFixesIterator(previous.getTimePoint(), /* inclusive */ true);
        final Pair<GPSFixMoving, Double> ascendingBestMatch = findBestMatch(fix, ascendingIterator);
        final Iterator<GPSFixMoving> descendingIterator = track.getFixesDescendingIterator(fix.getTimePoint(), /* inclusive */ false);
        final Pair<GPSFixMoving, Double> descendingBestMatch = findBestMatch(fix, descendingIterator);
        // Use the greater of the two offsets; the lesser will link it to its own sub-sequence neighbor
        return ascendingBestMatch != null && (descendingBestMatch == null || ascendingBestMatch.getB().compareTo(descendingBestMatch.getB()) < 0) ?
                ascendingBestMatch : descendingBestMatch;
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
    private Pair<GPSFixMoving, Double> findBestMatch(final GPSFixMoving fix, final Iterator<GPSFixMoving> iterator) {
        final Position fixPosition = fix.getPosition();
        GPSFixMoving lastFix = null;
        GPSFixMoving result = null;
        Distance minimum = new MeterDistance(Double.MAX_VALUE);
        boolean foundMinimum = false;
        Double distanceRatio = null;
        while (!foundMinimum && iterator.hasNext()) {
            final GPSFixMoving currentFix = iterator.next();
            if (currentFix != fix) { // skip the outlier fix itself
                if (lastFix != null) {
                    final Distance distanceFromSegment = fixPosition.getDistanceToLine(lastFix.getPosition(), currentFix.getPosition()).abs();
                    if (distanceFromSegment.compareTo(minimum) < 0) {
                        minimum = distanceFromSegment;
                        final Bearing bearingFromLastToCurrent = lastFix.getPosition().getBearingGreatCircle(currentFix.getPosition());
                        final Distance alongTrackDistanceFromLastFix = fixPosition.alongTrackDistance(lastFix.getPosition(), bearingFromLastToCurrent);
                        // interpolate the time between the adjacent fixes to whose connection "fix" is closest, splitting the duration
                        // between the adjacent fixes proportionately based on "fix"'s distances to each of the two adjacent fixes:
                        final TimePoint inferredTimePointForFix = lastFix.getTimePoint().plus(lastFix.getTimePoint().until(currentFix.getTimePoint()).times(
                                alongTrackDistanceFromLastFix.divide(lastFix.getPosition().getDistance(currentFix.getPosition()))));
                        result = new GPSFixMovingImpl(fixPosition, inferredTimePointForFix, fix.getSpeed());
                        distanceRatio = distanceFromSegment.divide(lastFix.getPosition().getDistance(currentFix.getPosition()));
                    } else { // we found a minimum after fix:
                        foundMinimum = true;
                    }
                }
                lastFix = currentFix;
            }
        }
        return foundMinimum ? new Pair<>(result, distanceRatio) : null;
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
