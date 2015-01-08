package com.sap.sailing.domain.swisstimingreplayadapter.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Sideline;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.TrackedRaceStatusEnum;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.WindSourceWithAdditionalID;
import com.sap.sailing.domain.racelog.impl.EmptyRaceLogStore;
import com.sap.sailing.domain.racelog.tracking.EmptyGPSFixStore;
import com.sap.sailing.domain.regattalog.impl.EmptyRegattaLogStore;
import com.sap.sailing.domain.swisstimingadapter.DomainFactory;
import com.sap.sailing.domain.swisstimingreplayadapter.CompetitorStatus;
import com.sap.sailing.domain.swisstimingreplayadapter.SwissTimingReplayListener;
import com.sap.sailing.domain.swisstimingreplayadapter.SwissTimingReplayParser;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.tracking.impl.MarkPassingImpl;
import com.sap.sailing.domain.tracking.impl.TrackedRaceStatusImpl;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

import difflib.PatchFailedException;

/**
 * Turns the data received through the {@link SwissTimingReplayListener} callback interface into domain objects, creating
 * a {@link RaceDefinition} and {@link Competitor}s as well as a {@link Course} with {@link Waypoint}s and {@link Mark}s.
 * Also, this adapter creates a {@link TrackedRace} for the race and records all tracked positions for competitors and
 * marks as well as wind data in it.<p>
 * 
 * This adapter is stateful and not thread safe. It must be used only once for registering with a {@link SwissTimingReplayParser}.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class SwissTimingReplayToDomainAdapter extends SwissTimingReplayAdapter {
    private static final int THRESHOLD_FOR_EARLIEST_MARK_PASSING_BEFORE_START_IN_MILLIS = 30000;

    private static final Logger logger = Logger.getLogger(SwissTimingReplayToDomainAdapter.class.getName());

    private final DomainFactory domainFactory;

    private final Map<String, RaceDefinition> racePerRaceID;
    private final Map<String, DynamicTrackedRace> trackedRacePerRaceID;

    /**
     * The last race ID received from {@link #raceID(String)}. Used as key into {@link #racePerRaceID} and
     * {@link #trackedRacePerRaceID} for storing data from subsequent messages.
     */
    private String currentRaceID;
    
    /**
     * Reference time point for time specifications
     */
    private TimePoint referenceTimePoint;

    /**
     * feference location for location / lat/lng specifications
     */
    private Position referenceLocation;

    private final Map<String, Set<Competitor>> competitorsPerRaceID;

    private final Map<String, Map<String, Mark>> marksPerRaceIDPerMarkID;

    private final Map<String, TimePoint> bestStartTimePerRaceID;

    private final Map<String, TimePoint> raceTimePerRaceID;

    /**
     * When the first mark definition of a course sequence is received, this member holds <code>null</code> and is then
     * initialized with a valid list. The list then accumulates the marks until the tracker count is received which marks
     * the end of the course sequence and the beginning of the tracker messages. The course is then compared to the current
     * course. If the race hasn't even been created, this can be done now. Otherwise, the new course definition is compared
     * to the current race's course definition, and if necessary, a course change is performed. Then, this member is set
     * to <code>null</code> again to show that the course has been consumed and updated to the race definition if necessary.
     */
    private List<ControlPoint> currentCourseDefinition;

    private final Regatta regatta;

    private final TrackedRegattaRegistry trackedRegattaRegistry;

    private final Map<Integer, Mark> markByHashValue;

    private final Map<Integer, Competitor> competitorByHashValue;

    /**
     * If a wind speed and direction was transmitted with the last mark message, it is recorded for the mark. When the next
     * mark position tracker message is received, a look up in this map is performed. If a wind speed/direction is found, it is
     * added to the tracked race's {@link WindSourceType#EXPEDITION} wind track.
     */
    private final Map<ControlPoint, SpeedWithBearing> windAtControlPoint;

    /**
     * Records the next mark for each competitor numerically
     */
    private final Map<Competitor, Short> lastNextMark;

    private RaceStatus lastRaceStatus;

    /**
     * @param regatta
     *            the regatta to associate the race(s) received by the listener with, or <code>null</code> to force the
     *            use / creation of a default regatta per race
     */
    public SwissTimingReplayToDomainAdapter(Regatta regatta, DomainFactory domainFactory, TrackedRegattaRegistry trackedRegattaRegistry) {
        this.regatta = regatta;
        this.trackedRegattaRegistry = trackedRegattaRegistry;
        racePerRaceID = new HashMap<>();
        trackedRacePerRaceID = new HashMap<>();
        bestStartTimePerRaceID = new HashMap<>();
        raceTimePerRaceID = new HashMap<>();
        competitorsPerRaceID = new HashMap<>();
        marksPerRaceIDPerMarkID = new HashMap<>();
        markByHashValue = new HashMap<>();
        competitorByHashValue = new HashMap<>();
        windAtControlPoint = new HashMap<>();
        lastNextMark = new HashMap<>();
        this.domainFactory = domainFactory;
    }

    public Iterable<DynamicTrackedRace> getTrackedRaces() {
        return trackedRacePerRaceID.values();
    }

    @Override
    public void referenceTimestamp(long referenceTimestampMillis) {
        referenceTimePoint = new MillisecondsTimePoint(referenceTimestampMillis);
    }

    @Override
    public void referenceLocation(int latitude, int longitude) {
        referenceLocation = new DegreePosition(((double) latitude)/10000000., ((double) longitude)/10000000.); 
    }

    @Override
    public void raceID(String raceID) {
        currentRaceID = raceID;
    }

    private boolean isValid(int threeByteValue) {
        return threeByteValue != (1<<24) - 1;
    }

    @Override
    public void frameMetaData(byte cid, int raceTime, int startTime, int estimatedStartTime, RaceStatus raceStatus,
            short distanceToNextMark, Weather weather, short humidity, short temperature, String messageText,
            byte cFlag, byte rFlag, byte duration, short nextMark) {
        if (raceStatus != lastRaceStatus) {
            lastRaceStatus = raceStatus;
            if (raceStatus == RaceStatus.running) {
                // remove all previous mark passings if the tracked race already exists; it may contain mark passings
                // from previous, aborted runs of the same race
                DynamicTrackedRace trackedRace = trackedRacePerRaceID.get(currentRaceID);
                if (trackedRace != null) {
                    Iterable<MarkPassing> noMarkPassings = Collections.emptyList();
                    for (Competitor competitor : trackedRace.getRace().getCompetitors()) {
                        trackedRace.updateMarkPassings(competitor, noMarkPassings);
                    }
                }
            }
        }
        if (isValid(startTime)) {
            final TimePoint startTimeReceived = getTimePoint(startTime);
            bestStartTimePerRaceID.put(currentRaceID, startTimeReceived);
            DynamicTrackedRace trackedRace = trackedRacePerRaceID.get(currentRaceID);
            if (trackedRace != null) {
                trackedRace.setStartTimeReceived(startTimeReceived);
            }
        } else if (isValid(estimatedStartTime)) {
            bestStartTimePerRaceID.put(currentRaceID, getTimePoint(estimatedStartTime));
        }
        if (isValid(raceTime) && bestStartTimePerRaceID.containsKey(currentRaceID)) {
            raceTimePerRaceID.put(currentRaceID, bestStartTimePerRaceID.get(currentRaceID).plus(1000*raceTime));
        }
    }

    private TimePoint getTimePoint(int threeByteTimeSpecInSecondsSinceMidnight) {
        assert isValid(threeByteTimeSpecInSecondsSinceMidnight);
        return getMidnight(referenceTimePoint).plus(1000 * threeByteTimeSpecInSecondsSinceMidnight);
    }

    private TimePoint getMidnight(TimePoint referenceTimePoint) {
        // FIXME need to identify time zone based on date and location
        GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.setTimeInMillis(referenceTimePoint.asMillis());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new MillisecondsTimePoint(cal.getTimeInMillis());
    }

    @Override
    public void competitor(int hashValue, String threeLetterIOCCode, String sailNumberOrTrackerID, String name,
            CompetitorStatus competitorStatus, BoatType boatType, short cRank_Bracket, short cnPoints_x10_Bracket,
            short ctPoints_x10_Winner) {
        if (boatType == BoatType.Competitor) {
            Competitor competitor = domainFactory.createCompetitorWithoutID(sailNumberOrTrackerID, threeLetterIOCCode.trim(), name.trim(),
                    currentRaceID, domainFactory.getRaceTypeFromRaceID(currentRaceID).getBoatClass());
            Set<Competitor> competitorsOfCurrentRace = competitorsPerRaceID.get(currentRaceID);
            if (competitorsOfCurrentRace == null) {
                competitorsOfCurrentRace = new HashSet<>();
                competitorsPerRaceID.put(currentRaceID, competitorsOfCurrentRace);
            }
            competitorsOfCurrentRace.add(competitor);
            competitorByHashValue.put(hashValue, competitor);
        } else {
            // consider it a mark
            Mark mark = domainFactory.getOrCreateMark(sailNumberOrTrackerID.trim());
            Map<String, Mark> marksOfCurrentRace = marksPerRaceIDPerMarkID.get(currentRaceID);
            if (marksOfCurrentRace == null) {
                marksOfCurrentRace = new HashMap<>();
                marksPerRaceIDPerMarkID.put(currentRaceID, marksOfCurrentRace);
            }
            marksOfCurrentRace.put(sailNumberOrTrackerID.trim(), mark);
            markByHashValue.put(hashValue, mark);
        }
    }


    @Override
    public void mark(MarkType markType, String name, byte index, String id1, String id2, short windSpeedInKnots,
            short trueWindDirectionInDegrees) {
        final List<String> markNames = new ArrayList<>();
        markNames.add(id1.trim());
        if (id2 != null) {
            markNames.add(id2.trim());
        }
        final ControlPoint controlPoint = domainFactory.getOrCreateControlPoint(markNames);
        if (index == 0) {
            currentCourseDefinition = new ArrayList<>();
        }
        while (currentCourseDefinition.size() < index+1) {
            currentCourseDefinition.add(null); // pad course
        }
        currentCourseDefinition.set(index, controlPoint);
        if (windSpeedInKnots != -1 && trueWindDirectionInDegrees != -1) {
            SpeedWithBearing windSpeedWithBearing = new KnotSpeedWithBearingImpl(windSpeedInKnots,
                    new DegreeBearingImpl(trueWindDirectionInDegrees).reverse());
            windAtControlPoint.put(controlPoint, windSpeedWithBearing);
        } else {
            windAtControlPoint.remove(controlPoint);
        }
    }

    @Override
    public void trackersCount(short trackersCount) {
        try {
            RaceDefinition race = racePerRaceID.get(currentRaceID);
            if (race == null) {
                createRace();
            } else {
                Course course = race.getCourse();
                try {
                    // TODO: Does SwissTiming also deliver the passing side for course marks?
                    List<com.sap.sse.common.Util.Pair<ControlPoint, PassingInstruction>> courseToUpdate = new ArrayList<com.sap.sse.common.Util.Pair<ControlPoint, PassingInstruction>>();
                    for (ControlPoint cp : currentCourseDefinition) {
                        courseToUpdate.add(new com.sap.sse.common.Util.Pair<ControlPoint, PassingInstruction>(cp, null));
                    }
                    course.update(courseToUpdate, domainFactory.getBaseDomainFactory());
                } catch (PatchFailedException e) {
                    throw new RuntimeException(e);
                }
            }
        } finally {
            currentCourseDefinition = null;
        }
    }

    private void createRace() {
        final Regatta myRegatta = regatta != null ? regatta : domainFactory.getOrCreateDefaultRegatta(
                EmptyRaceLogStore.INSTANCE, EmptyRegattaLogStore.INSTANCE,
                currentRaceID, domainFactory.getRaceTypeFromRaceID(currentRaceID).getBoatClass(), trackedRegattaRegistry);
        RaceDefinition race = domainFactory.createRaceDefinition(myRegatta,
                currentRaceID, competitorsPerRaceID.get(currentRaceID), currentCourseDefinition);
        racePerRaceID.put(currentRaceID, race);
        DynamicTrackedRace trackedRace = trackedRegattaRegistry.getOrCreateTrackedRegatta(myRegatta).
                createTrackedRace(race, Collections.<Sideline> emptyList(), EmptyWindStore.INSTANCE, EmptyGPSFixStore.INSTANCE,TrackedRace.DEFAULT_LIVE_DELAY_IN_MILLISECONDS,
                        WindTrack.DEFAULT_MILLISECONDS_OVER_WHICH_TO_AVERAGE_WIND, 
                        /* time over which to average speed: */ race.getBoatClass().getApproximateManeuverDurationInMilliseconds(),
                        /* raceDefinitionSetToUpdate */ null,/*useMarkPassingCalculator*/ false);
        trackedRace.setStatus(new TrackedRaceStatusImpl(TrackedRaceStatusEnum.LOADING, 0));
        TimePoint bestStartTimeKnownSoFar = bestStartTimePerRaceID.get(currentRaceID);
        if (bestStartTimeKnownSoFar != null) {
            trackedRace.setStartTimeReceived(bestStartTimeKnownSoFar);
        }
        trackedRacePerRaceID.put(currentRaceID, trackedRace);
    }

    @Override
    public void trackers(int hashValue, int latitude, int longitude, short cog, short sog_Knots_x10, short average_sog,
            short vmg_Knots_x10, CompetitorStatus competitorStatus, short rank, short distanceToLeader_meters,
            short distanceToNextMark_meters, short nextMark, short pRank, short ptPoints, short pnPoints) {
        DynamicTrackedRace trackedRace = trackedRacePerRaceID.get(currentRaceID);
        Position position = new DegreePosition(referenceLocation.getLatDeg() - ((double) latitude) / 10000000.,
                referenceLocation.getLngDeg() - ((double) longitude) / 10000000.);
        TimePoint raceTimePoint = raceTimePerRaceID.containsKey(currentRaceID) ? raceTimePerRaceID.get(currentRaceID) : referenceTimePoint;
        Bearing bearing = new DegreeBearingImpl(cog);
        SpeedWithBearing speed = new KnotSpeedWithBearingImpl(((double) sog_Knots_x10) / 10., bearing);
        GPSFixMoving fix = new GPSFixMovingImpl(position, raceTimePoint, speed);
        Mark mark = markByHashValue.get(hashValue);
        if (mark != null) {
            Iterator<Entry<ControlPoint, SpeedWithBearing>> i = windAtControlPoint.entrySet().iterator();
            while (i.hasNext()) {
                Entry<ControlPoint, SpeedWithBearing> controlPointWithWind = i.next();
                if (Util.contains(controlPointWithWind.getKey().getMarks(), mark)) {
                    trackedRace.recordWind(new WindImpl(position, raceTimePoint, controlPointWithWind.getValue()),
                            new WindSourceWithAdditionalID(WindSourceType.EXPEDITION, mark.getName()));
                    i.remove();
                }
            }
            trackedRace.recordFix(mark, fix);
        } else {
            Competitor competitor = competitorByHashValue.get(hashValue);
            if (competitor != null) {
                trackedRace.recordFix(competitor, fix);
                Course course = trackedRace.getRace().getCourse();
                // record a mark passing, but not if the mark passing has happened longer than 30s before the race start
                if (bestStartTimePerRaceID.get(currentRaceID) != null &&
                        !bestStartTimePerRaceID.get(currentRaceID).after(
                                raceTimePoint.plus(THRESHOLD_FOR_EARLIEST_MARK_PASSING_BEFORE_START_IN_MILLIS)) &&
                                (!lastNextMark.containsKey(competitor) || lastNextMark.get(competitor) != nextMark) && nextMark > 0) {
                    Waypoint waypointPassed = nextMark == 255 ? course.getLastWaypoint() : Util.get(course.getWaypoints(), nextMark - 1);
                    List<MarkPassing> newMarkPassings = new ArrayList<>();
                    final NavigableSet<MarkPassing> markPassings = trackedRace.getMarkPassings(competitor);
                    trackedRace.lockForRead(markPassings);
                    try {
                        Util.addAll(markPassings, newMarkPassings);
                    } finally {
                        trackedRace.unlockAfterRead(markPassings);
                    }
                    newMarkPassings.add(new MarkPassingImpl(raceTimePoint, waypointPassed, competitor));
                    trackedRace.updateMarkPassings(competitor, newMarkPassings);
                    lastNextMark.put(competitor, nextMark);
                }
            } else {
                logger.warning("Couldn't find hash value "+hashValue+" in either the mark or the competitor map");
            }
        }
    }

    @Override
    public void progress(double progress) {
        DynamicTrackedRace trackedRace = trackedRacePerRaceID.get(currentRaceID);
        if (trackedRace != null) {
            final TrackedRaceStatusEnum newStatus;
            if (progress == 1.0) {
                newStatus = TrackedRaceStatusEnum.FINISHED;
            } else {
                newStatus = TrackedRaceStatusEnum.LOADING;
            }
            trackedRace.setStatus(new TrackedRaceStatusImpl(newStatus, progress));
        }
    }
}
