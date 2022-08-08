package com.sap.sailing.server.trackfiles.test;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

import org.junit.Test;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorWithBoat;
import com.sap.sailing.domain.base.ControlPointWithTwoMarks;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.BoatImpl;
import com.sap.sailing.domain.base.impl.ControlPointWithTwoMarksImpl;
import com.sap.sailing.domain.base.impl.CourseAreaImpl;
import com.sap.sailing.domain.base.impl.CourseImpl;
import com.sap.sailing.domain.base.impl.DynamicBoat;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.base.impl.MarkImpl;
import com.sap.sailing.domain.base.impl.RaceDefinitionImpl;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.base.impl.SeriesImpl;
import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.domain.common.CompetitorRegistrationType;
import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.common.tracking.impl.GPSFixImpl;
import com.sap.sailing.domain.common.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.leaderboard.impl.LowPoint;
import com.sap.sailing.domain.racelog.RaceLogAndTrackedRaceResolver;
import com.sap.sailing.domain.ranking.OneDesignRankingMetric;
import com.sap.sailing.domain.test.AbstractLeaderboardTest;
import com.sap.sailing.domain.test.DummyTrackedRegattaRegistry;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.impl.DynamicGPSFixMovingTrackImpl;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRaceImpl;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRegattaImpl;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.server.trackfiles.RouteConverterGPSFixImporterFactory;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Color;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;

import difflib.PatchFailedException;

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
    private static class Inconsistency {
        private final GPSFixMoving previous;
        private final GPSFixMoving fix;
        private final GPSFixMoving next;
        private final double SPEED_RATIO_TOLERANCE;
        private final double COURSE_DEGREE_TOLERANCE;
        
        public Inconsistency(GPSFixMoving previous, GPSFixMoving fix, GPSFixMoving next, double SPEED_RATIO_TOLERANCE, double COURSE_DEGREE_TOLERANCE) {
            super();
            this.previous = previous;
            this.fix = fix;
            this.next = next;
            this.SPEED_RATIO_TOLERANCE = SPEED_RATIO_TOLERANCE;
            this.COURSE_DEGREE_TOLERANCE = COURSE_DEGREE_TOLERANCE;
        }

        public SpeedWithBearing getInferredBetweenPreviousAndFix() {
            return previous.getSpeedAndBearingRequiredToReach(fix);
        }
        
        public SpeedWithBearing getInferredBetweenFixAndNext() {
            return fix.getSpeedAndBearingRequiredToReach(next);
        }
        
        public SpeedWithBearing getReportedByPrevious() {
            return previous.getSpeed();
        }
        
        public SpeedWithBearing getReportedByFix() {
            return fix.getSpeed();
        }
        
        public SpeedWithBearing getReportedByNext() {
            return next.getSpeed();
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("previous:     ");
            sb.append(previous);
            sb.append('\n');
            sb.append("fix:          ");
            sb.append(fix);
            sb.append('\n');
            sb.append("next:         ");
            sb.append(next);
            sb.append('\n');
            sb.append("previous-fix: ");
            sb.append(getInferredBetweenPreviousAndFix());
            sb.append('\n');
            sb.append("fix-next      ");
            sb.append(getInferredBetweenFixAndNext());
            sb.append('\n');
            
            if (!isConsistent(getReportedByPrevious(), getInferredBetweenPreviousAndFix(), SPEED_RATIO_TOLERANCE, COURSE_DEGREE_TOLERANCE)) {
                sb.append("Inconsistent between reported by previous and inferred between previous and fix\n");
            }
            if (!isConsistent(getInferredBetweenFixAndNext(), getReportedByFix(), SPEED_RATIO_TOLERANCE, COURSE_DEGREE_TOLERANCE)) {
                sb.append("Inconsistent between inferred between fix and next and reported by fix\n");
            }
            if (!isConsistent(getInferredBetweenFixAndNext(), getReportedByNext(), SPEED_RATIO_TOLERANCE, COURSE_DEGREE_TOLERANCE)) {
                sb.append("Inconsistent between inferred between fix and next and reported by next\n");
            }
            return sb.toString();
        }
    }
    
    @Test
    public void testMarkPassingCalculatorForOriginal() throws Exception {
        final DynamicGPSFixTrack<Competitor, GPSFixMoving> track = readTrack("GallagherZelenka.gpx.gz");
        final DynamicTrackedRace trackedRace = createRace(track);
        final NavigableSet<MarkPassing> markPassings = trackedRace.getMarkPassings(track.getTrackedItem(), /* wait for latest update */ true);
        assertNotNull(markPassings);
    }
    
    @Test
    public void testMarkPassingCalculatorForAdjusted() throws Exception {
        final DynamicGPSFixTrack<Competitor, GPSFixMoving> track = readTrack("GallagherZelenka.gpx.gz");
        final Pair<Integer, DynamicGPSFixTrack<Competitor, GPSFixMoving>> replaced = findAndRemoveInconsistenciesOnRawFixes(track);
        final Competitor competitor = track.getTrackedItem();
        final DynamicTrackedRace trackedRace = createRace(replaced.getB());
        final NavigableSet<MarkPassing> markPassings = trackedRace.getMarkPassings(competitor, /* wait for latest update */ true);
        assertNotNull(markPassings);
    }
    
    private DynamicGPSFixTrack<Competitor, GPSFixMoving> readTrack(String filename) throws Exception {
        final DynamicBoat boat = new BoatImpl("1", "1", new BoatClassImpl(BoatClassMasterdata.MELGES_24), /* sailID */ "1");
        final DynamicGPSFixTrack<Competitor, GPSFixMoving> track = new DynamicGPSFixMovingTrackImpl<Competitor>(AbstractLeaderboardTest.createCompetitorWithBoat(filename, boat),
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
        return track;
    }
    
    /**
     * Simulates the "Oak cliff DH Distance Race" R1 with a single competitor, Gallagher / Zelenka, sail number "1" with
     * the marks pinged statically to establish the course. The track of Gallagher / Zelenka is provided as a track of
     * their GPS positions. This could be the raw track, or it may be a filtered variant of the track with outliers
     * removed or adjusted.<p>
     * 
     * The race that is returned with have the mark passing calculator activated, and a test may wait for it to complete
     * its calculation. As a result, a test may determine the impact filtering / adjusting the track may have on the
     * mark passing analysis.
     */
    private DynamicTrackedRace createRace(DynamicGPSFixTrack<Competitor, GPSFixMoving> competitorTrack) throws PatchFailedException, ParseException {
        final Competitor gallagherZelenka = competitorTrack.getTrackedItem();
        final DynamicTrackedRace trackedRace = createTrackedRace("Oak cliff DH Distance Race", "R1", BoatClassMasterdata.MELGES_24, gallagherZelenka);
        trackedRace.setStartOfTrackingReceived(TimePoint.of(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX").parse("2020-10-14T17:00:00Z")));
//        final Mark lisR32a = createAndPlaceMark(trackedRace, "32A - Mid-Sound Buoy", "LIS R32A", 40.96866998355836, -73.54664996266365, MarkType.BUOY, Color.ofRgb("#FF0000"), "CYLINDER");
//        final Mark lisC17 = createAndPlaceMark(trackedRace, "C17 - Sound Side of Center Island Green", "LIS C17", 40.93856998253614, -73.53271999396384, MarkType.BUOY, Color.ofRgb("#008000"), "CYLINDER");
        final Mark cshG1 = createAndPlaceMark(trackedRace, "Cold Spring Harbor G1", "CSH G1", 40.92594997957349, -73.50404994562268, MarkType.BUOY, Color.ofRgb("#008000"), "CYLINDER");
        final Mark cshl = createAndPlaceMark(trackedRace, "Cold Spring Harbor Light", "CSHL", 40.91418300289661, -73.4931492805481, MarkType.BUOY, null, "CYLINDER");
        final Mark finishBoat = createAndPlaceMark(trackedRace, "Finish Boat", "FB", 40.89873938821256, -73.51117020472884, MarkType.FINISHBOAT, null, null);
        final Mark finishPin = createAndPlaceMark(trackedRace, "Finish Pin", "FP", 40.897511816583574, -73.50983932614326, MarkType.BUOY, null, null);
        final Mark faulkner = createAndPlaceMark(trackedRace, "G15 - North Side Faulkner Island", "Faulkner", 40.96866998355836, -73.54664996266365, MarkType.BUOY, Color.ofRgb("#008000"), "CONICAL");
//        final Mark bayville = createAndPlaceMark(trackedRace, "LIS G19 - Bayville", "Bayville", 40.92419996391982, -73.56988325715065, MarkType.BUOY, Color.ofRgb("#008000"), "CONICAL");
        final Mark matinecock = createAndPlaceMark(trackedRace, "LIS G21 - Matinecock Pt", "Matinecock Pt", 40.90974998194724, -73.63691660575569, MarkType.BUOY, Color.ofRgb("#008000"), "CONICAL");
        final Mark sixMileReef = createAndPlaceMark(trackedRace, "LIS R8C - 6 Mile Reef", "6 Mile Reef", 41.17991665843874, -72.49066662043333, MarkType.BUOY, Color.ofRgb("#FF0000"), "CONICAL");
        final Mark newMark = createAndPlaceMark(trackedRace, "New Mark", "NM", 40.924666626378894, -73.70251664891839, MarkType.BUOY, null, null);
//        final Mark ob2 = createAndPlaceMark(trackedRace, "Oyster Bay Buoy 2", "OB2", 40.91139995958656, -73.50232997909188, MarkType.BUOY, Color.ofRgb("#FF0000"), "CONICAL");
//        final Mark ob4 = createAndPlaceMark(trackedRace, "Oyster Bay Buoy 4", "OB4", 40.90192995965481, -73.50629998371005, MarkType.BUOY, Color.ofRgb("#FF0000"), "CONICAL");
//        final Mark ob5 = createAndPlaceMark(trackedRace, "Oyster Bay Buoy 5 (Seawanhaka)", "OB5", 40.89752623345703, -73.50977637805045, MarkType.BUOY, Color.ofRgb("#008000"), "CYLINDER");
        final Mark cows = createAndPlaceMark(trackedRace, "R32 - The Cows", "Cows", 41.003599972464144, -73.52359998039901, MarkType.BUOY, Color.ofRgb("#FF0000"), "CONICAL");
        final Mark startBoat = createAndPlaceMark(trackedRace, "Start Boat", "SB", 40.8984215464443, -73.51104154251516, MarkType.STARTBOAT, null, null);
        final Mark startPin = createAndPlaceMark(trackedRace, "Start Pin", "SP", 40.89739736169577, -73.50981149822474, MarkType.BUOY, null, null);
//        final Mark cowes32 = createAndPlaceMark(trackedRace, "The Cowes Lighted Bell Buoy 32", "Cowes 32", 40.00361998099834, -73.52387993596494, MarkType.BUOY, null, null);
        final ControlPointWithTwoMarks start = new ControlPointWithTwoMarksImpl(startBoat, startPin, "Start", "S");
        final ControlPointWithTwoMarks finish = new ControlPointWithTwoMarksImpl(finishBoat, finishPin, "Finish", "F");
        trackedRace.getRace().getCourse().update(Arrays.asList(
                new Pair<>(start, PassingInstruction.Line),
                new Pair<>(cshl, PassingInstruction.Port),
                new Pair<>(cshG1, PassingInstruction.Port),
                new Pair<>(cshl, PassingInstruction.Port),
                new Pair<>(cows, PassingInstruction.Starboard),
                new Pair<>(faulkner, PassingInstruction.Starboard),
                new Pair<>(sixMileReef, PassingInstruction.Starboard),
                new Pair<>(cows, PassingInstruction.Port),
                new Pair<>(newMark, PassingInstruction.Port),
                new Pair<>(matinecock, PassingInstruction.Port),
                new Pair<>(cows, PassingInstruction.Starboard),
                new Pair<>(cshl, PassingInstruction.Starboard),
                new Pair<>(finish, PassingInstruction.Line)),
                /* associatedRoles */ Collections.emptyMap(), /* originatingCouseTemplateIdOrNull */ null, DomainFactory.INSTANCE);
        final DynamicGPSFixTrack<Competitor, GPSFixMoving> competitorTrackInRace = trackedRace.getTrack(gallagherZelenka);
        // TODO switch race into suspended mode to avoid updates during mass fix insertion:
        competitorTrack.lockForRead();
        try {
            for (final GPSFixMoving fix : competitorTrack.getRawFixes()) {
                competitorTrackInRace.add(fix);
            }
        } finally {
            competitorTrack.unlockAfterRead();
        }
        // TODO resume race
        return trackedRace;
    }
    
    private DynamicTrackedRace createTrackedRace(String regattaName, String name, BoatClassMasterdata boatClassMasterData, Competitor gallagherZelenka) {
        final BoatClassImpl boatClass = new BoatClassImpl(boatClassMasterData);
        final TrackedRegatta trackedRegatta = new DynamicTrackedRegattaImpl(new RegattaImpl(regattaName, boatClass,
                /* canBoatsOfCompetitorsChangePerRace */ false, /* competitorRegistrationType */ CompetitorRegistrationType.CLOSED,
                /* startDate */ null, /* endDate */ null, Collections.singleton(new SeriesImpl("Default", /* isMedal */ false, /* isFleetsCanRunInParallel */ false,
                        Collections.singleton(new FleetImpl("Default", 0)), Collections.singleton("R1"), new DummyTrackedRegattaRegistry())), /* persistent */ false,
                new LowPoint(), UUID.randomUUID(), new CourseAreaImpl("Default", UUID.randomUUID()), OneDesignRankingMetric::new,
                /* registrationLinkSecret */ null));
        final Boat boat = ((CompetitorWithBoat) gallagherZelenka).getBoat();
        final Map<Competitor, Boat> competitorsAndTheirBoats = Util.<Competitor, Boat>mapBuilder().put(gallagherZelenka, boat).build();
        final Course course = new CourseImpl("R1 Course", Collections.emptySet());
        final RaceDefinition race = new RaceDefinitionImpl(name, course, boatClass, competitorsAndTheirBoats, UUID.randomUUID());
        return new DynamicTrackedRaceImpl(trackedRegatta, race, /* sidelines */ Collections.emptySet(), new EmptyWindStore(), /* delayToLiveInMillis */ 1000,
                WindTrack.DEFAULT_MILLISECONDS_OVER_WHICH_TO_AVERAGE_WIND, /* time over which to average speed: */ boatClass.getApproximateManeuverDurationInMilliseconds(),
                /* useInternalMarkPassingAlgorithm */ true, OneDesignRankingMetric::new, mock(RaceLogAndTrackedRaceResolver.class), /* trackingConnectorInfo */ null);
    }

    private Mark createAndPlaceMark(DynamicTrackedRace trackedRace, String name, String shortName, double latDeg, double lngDeg,
            MarkType markType, Color color, String shape) {
        final Mark mark = new MarkImpl(UUID.randomUUID(), name, markType, color, shape, /* pattern */ null);
        final DynamicGPSFixTrack<Mark, GPSFix> markTrack = trackedRace.getOrCreateTrack(mark);
        final GPSFix markFix = new GPSFixImpl(new DegreePosition(latDeg, lngDeg), trackedRace.getStartOfTracking());
        markTrack.add(markFix);
        return mark;
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
        if (hasInconsistentCogSog(previous, fix, next, /* speed ratio tolerance */ 0.1, /* course degree tolerance */ 10)) {
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
    
    public static LinkedHashMap<GPSFixMoving, SpeedWithBearing> getInferredSpeeds(DynamicGPSFixTrack<Competitor, GPSFixMoving> track) {
        final LinkedHashMap<GPSFixMoving, SpeedWithBearing> inferredSpeeds = new LinkedHashMap<>();
        track.lockForRead();
        try {
            GPSFixMoving previous = null;
            for (final GPSFixMoving fix : track.getRawFixes()) {
                if (previous != null) {
                    inferredSpeeds.put(fix, previous.getSpeedAndBearingRequiredToReach(fix));
                }
                previous = fix;
            }
        } finally {
            track.unlockAfterRead();
        }
        return inferredSpeeds;
    }
    
    private boolean hasInconsistentCogSog(GPSFixMoving previous, GPSFixMoving fix, GPSFixMoving next, double SPEED_RATIO_TOLERANCE, double COURSE_DEGREE_TOLERANCE) {
        final SpeedWithBearing inferredBetweenPreviousAndFix = previous.getSpeedAndBearingRequiredToReach(fix);
        final SpeedWithBearing inferredBetweenFixAndNext = fix.getSpeedAndBearingRequiredToReach(next);
        final SpeedWithBearing reportedByPrevious = previous.getSpeed();
        final SpeedWithBearing reportedByFix = fix.getSpeed();
        final SpeedWithBearing reportedByNext = next.getSpeed();
        return isConsistent(reportedByPrevious, reportedByNext, SPEED_RATIO_TOLERANCE, COURSE_DEGREE_TOLERANCE)
                && !isConsistent(reportedByPrevious, inferredBetweenPreviousAndFix, SPEED_RATIO_TOLERANCE, COURSE_DEGREE_TOLERANCE)
                && !isConsistent(inferredBetweenFixAndNext, reportedByFix, SPEED_RATIO_TOLERANCE, COURSE_DEGREE_TOLERANCE)
                && !isConsistent(inferredBetweenFixAndNext, reportedByNext, SPEED_RATIO_TOLERANCE, COURSE_DEGREE_TOLERANCE);
    }
    
    private static boolean isConsistent(double ratio, double tolerance) {
        return ratio < 1+tolerance && ratio > 1-tolerance; 
    }

    private static boolean isConsistent(SpeedWithBearing a, SpeedWithBearing b, double SPEED_RATIO_TOLERANCE, double COURSE_DEGREE_TOLERANCE) {
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
     * On {@link #track} looks at adjacent fixes and compares the COG/SOG values reported by those fixes with the
     * COG/SOG value inferred from their position and time delta.
     * <p>
     * 
     * Hypothesis: we have a fix sequence that describes the trajectory of a sailing boat where some of the fixes have
     * an incorrect time point. The offset of these incorrect time points varies. In the particular case observed, all
     * regular fixes have a time point that is at a full second (UTC) with zero milliseconds, whereas all outliers have
     * a non-zero millisecond part that does not fit the otherwise very regular sampling rate.
     * <p>
     * 
     * Due to the irregularity of the offsets there is no point in trying to "learn" this offset. Instead, it's more
     * about recognizing the outliers which so far always seem to come as a single fix in a longer series of regular
     * fixes, and then finding a good time point adjustment so it matches the sequence.
     * <p>
     * 
     * With this in mind we would always have to look "both ways," trying to find out whether the fix originally had an
     * earlier or a later time point that would bring it closely in line with the other fixes. The fix does contain
     * valuable information despite its incorrect time point because it could indicate a deviation from the straight
     * line otherwise connecting the two adjacent fixes.
     * <p>
     * 
     * To approximate the correct time point we look for the track segment closest to the fix's position, then project
     * the fix onto it and split the segment's duration proportionately.
     * <p>
     * 
     * @return the number of inconsistencies found on the {@code track} passed, as well as a replacement track that has
     *         the outliers found adjusted
     */
    private Pair<Integer, DynamicGPSFixTrack<Competitor, GPSFixMoving>> findAndRemoveInconsistenciesOnRawFixes(DynamicGPSFixTrack<Competitor, GPSFixMoving> track) {
        int numberOfInconsistencies = 0;
        final DynamicGPSFixMovingTrackImpl<Competitor> replacedTrack = new DynamicGPSFixMovingTrackImpl<Competitor>(track.getTrackedItem(),
                /* millisecondsOverWhichToAverage */ 5000, /* losslessCompaction */ true);
        replacedTrack.suspendValidityCaching();
        GPSFixMoving previous = null, fix = null;
        track.lockForRead();
        try {
            for (final GPSFixMoving next : track.getRawFixes()) { // raw fixes with ascending reported time
                if (previous != null && fix != null) {
                    final Pair<GPSFixMoving, Double> adjusted = isLikelyOutlierWithCorrectableTimepoint(track, previous, fix, next);
                    if (adjusted != null) {
                        // TODO remember (previous, fix, next) as an outlier to move and do not insert into replacedTrack
                        // TODO then run the adjustment process (see method adjust(track, previous, fix, next)) with the reduced track
                        // TODO this way, contiguous outliers will less probably have a negative impact on adjusting the outliers
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
        return new Pair<>(numberOfInconsistencies, replacedTrack);
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

    private void adjustTrackAndAssertNoOutliersInResult(String trackFileName, int maximumNumberOfOutliersAllowed) throws Exception {
        final DynamicGPSFixTrack<Competitor, GPSFixMoving> track = readTrack(trackFileName);
        track.lockForRead();
        try {
            assertFalse(Util.isEmpty(track.getRawFixes()));
        } finally {
            track.unlockAfterRead();
        }
        final Pair<Integer, DynamicGPSFixTrack<Competitor, GPSFixMoving>> numberOfInconsistenciesAndReplacedTrack = findAndRemoveInconsistenciesOnRawFixes(track);
        assertTrue(numberOfInconsistenciesAndReplacedTrack.getA() > maximumNumberOfOutliersAllowed);
        assertTrue(getNumberOfFixesWithInconsistentCogSog(numberOfInconsistenciesAndReplacedTrack.getB()) <= maximumNumberOfOutliersAllowed);
    }
    
    /**
     * Count severe COG/SOG inconsistencies left; severely inconsistent means a speed difference between inferred and
     * reported of more than 500%, or a course inconsistency of more than 90 degrees.
     */
    private int getNumberOfFixesWithInconsistentCogSog(DynamicGPSFixTrack<Competitor, GPSFixMoving> track) {
        int inconsistencies = 0;
        final Map<GPSFixMoving, Inconsistency> inconsistentFixes = new LinkedHashMap<>();
        track.lockForRead();
        try {
            GPSFixMoving previous = null, fix = null;
            for (final GPSFixMoving next : track.getRawFixes()) {
                if (previous != null && fix != null && hasInconsistentCogSog(previous, fix, next, /* speed ratio tolerance */ 5, /* course degree tolerance */ 120)) {
                    inconsistencies++;
                    inconsistentFixes.put(fix, new Inconsistency(previous, fix, next, 5, 120));
                }
                previous = fix;
                fix = next;
            }
        } finally {
            track.unlockAfterRead();
        }
        return inconsistencies;
    }

    @Test
    public void testCZE2471() throws Exception {
        adjustTrackAndAssertNoOutliersInResult("CZE2471.gpx.gz", 15);
    }

    @Test
    public void testCZE2956() throws Exception {
        adjustTrackAndAssertNoOutliersInResult("CZE2956.gpx.gz", 6);
    }
    
    /**
     * See https://my.sapsailing.com/gwt/RaceBoard.html?regattaName=Oak+cliff+DH+Distance+Race&raceName=R1&leaderboardName=Oak+cliff+DH+Distance+Race&leaderboardGroupId=a3902560-6bfa-43be-85e1-2b82a4963416&eventId=bf48a59d-f2af-47b6-a2f7-a5b78b22b9f2&mode=FULL_ANALYSIS
     * for the original race.
     */
    @Test
    public void testGallagherZelenka() throws Exception {
        adjustTrackAndAssertNoOutliersInResult("GallagherZelenka.gpx.gz", 23);
    }
}
