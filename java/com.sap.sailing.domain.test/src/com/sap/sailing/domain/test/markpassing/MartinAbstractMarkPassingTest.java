package com.sap.sailing.domain.test.markpassing;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.test.OnlineTracTracBasedTest;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRaceImpl;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.domain.tractracadapter.ReceiverType;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.tractrac.model.lib.api.event.CreateModelException;
import com.tractrac.subscription.lib.api.SubscriberInitializationException;

/**
 * Tests the mark passing algorithm by using the data provided by an {@link OnlineTracTracBasedTest}. Subclasses may
 * implement different mark passing detection algorithms.
 * 
 * @author Martin Hanysz
 * 
 */
public abstract class MartinAbstractMarkPassingTest extends OnlineTracTracBasedTest {
    /**
     * How many milliseconds may the given and computed mark passings drift apart before being counted as a miss.
     */
    private static final int TIMEDELTA_TOLERANCE = 10000;

    /**
     * Reload the test data from the web?
     */
    private boolean forceReload = false;

    public MartinAbstractMarkPassingTest() throws MalformedURLException, URISyntaxException {
        super();
    }

    public void setUp() throws IOException, InterruptedException, URISyntaxException, ParseException, SubscriberInitializationException, CreateModelException {
        super.setUp();
        String raceID = "357c700a-9d9a-11e0-85be-406186cbf87c";
        if (!loadData(raceID) && !forceReload) {
            System.out.println("Downloading new data from the web.");
            this.setUp("event_20110609_KielerWoch",
            /* raceId */raceID, new ReceiverType[] { ReceiverType.MARKPASSINGS, ReceiverType.MARKPOSITIONS,
                    ReceiverType.RACECOURSE, ReceiverType.RAWPOSITIONS });
            OnlineTracTracBasedTest.fixApproximateMarkPositionsForWindReadOut(getTrackedRace(),
                    new MillisecondsTimePoint(new GregorianCalendar(2011, 05, 23).getTime()));
            getTrackedRace().recordWind(
                    new WindImpl(/* position */null, MillisecondsTimePoint.now(), new KnotSpeedWithBearingImpl(12,
                            new DegreeBearingImpl(65))), new WindSourceImpl(WindSourceType.WEB));
            saveData();
        }
    }

    /**
     * Loads stored data for the given raceID or returns false if no data is present.
     * 
     * @param raceID
     *            - ID of the race to load from disk
     * @return true if data was loaded, false if not
     */
    private boolean loadData(String raceID) {
        String path = null;
        File file = new File("resources/");
        if (file.exists() && file.isDirectory()) {
            for (String fileName : file.list()) {
                if (fileName.endsWith(".data") && fileName.contains(raceID)) {
                    path = "resources/" + fileName;
                    break;
                }
            }
        }
        if (path == null)
            return false;
        FileInputStream fs = null;
        ObjectInputStream os = null;
        Object obj = null;

        try {
            System.out.print("Loading cached data for raceID " + raceID + "...");
            fs = new FileInputStream(path);
            os = new ObjectInputStream(fs);
            obj = os.readObject();
            System.out.println("done!");
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
            if (fs != null) {
                try {
                    fs.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }

        if (obj != null && obj instanceof DynamicTrackedRaceImpl) {
            setTrackedRace((DynamicTrackedRaceImpl) obj);
            setRace(getTrackedRace().getRace());
            return true;
        }
        return false;
    }

    /**
     * Saves current result of getTrackedRace to disk for future reuse.
     */
    private void saveData() {
        DynamicTrackedRace trackedRace = getTrackedRace();
        String racePath = "resources/" + trackedRace.getRace().getId() + ".data";
        FileOutputStream fs = null;
        ObjectOutputStream os = null;
        try {
            System.out.println("Caching data for raceID " + trackedRace.getRace().getId());
            File f = new File(racePath);
            f.createNewFile();
            fs = new FileOutputStream(f);
            os = new ObjectOutputStream(fs);
            os.writeObject(trackedRace);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
            if (fs != null) {
                try {
                    fs.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    public void testMarkPassings() {
        List<MarkPassing> markPassings = computeAllMarkPassings();
        // compare computed mark passings to given ones
        int overallMisses = compareCalculatedAndGivenPassings(markPassings, true);
        assertEquals("Calculation returned less mark passings than the test data contains", 0, overallMisses);
    }

    /**
     * This method starts the actual mark passing detection algorithm by handing a sequence of {@link GPSFix}es of each
     * {@link Competitor} into the {@link MartinAbstractMarkPassingTest#computeMarkPassings(Competitor, GPSFixMoving)}
     * method. For every competitor, sequences of mark passings for each waypoint of the course can be calculated. One
     * sequence may contain multiple possible mark passings for the same waypoint that may represent the same passing of
     * that specific waypoint. The list of sequences returned in the result should contain one sequence for each time
     * the waypoint has to be passed to complete the course.
     * 
     * @return a {@link Map} from each {@link Competitor} to a {@link Map} from each {@link Waypoint} to a {@link List}
     *         of mark passing sequences
     */
    protected List<MarkPassing> computeAllMarkPassings() {
        ArrayList<MarkPassing> markPassings = new ArrayList<MarkPassing>();

        // To imitate GPSFixes that are received over time, we hand the GPSFixes to the detection algorithm in
        // chronological order
        HashMap<Competitor, Iterator<GPSFixMoving>> fixesIterators = new HashMap<Competitor, Iterator<GPSFixMoving>>();
        HashMap<Competitor, GPSFixMoving> nextFixes = new HashMap<Competitor, GPSFixMoving>();

        // get all the GPSFix iterators
        for (Competitor competitor : getRace().getCompetitors()) {
            try {
                getTrackedRace().getTrack(competitor).lockForRead();
                // from the start of tracking or "the dawn of time"
                TimePoint start = getTrackedRace().getStartOfTracking() != null ? getTrackedRace().getStartOfTracking()
                        : new MillisecondsTimePoint(0);
                fixesIterators.put(competitor, getTrackedRace().getTrack(competitor).getRawFixesIterator(start, true));
            } finally {
                getTrackedRace().getTrack(competitor).unlockAfterRead();
            }
        }

        // initially fill the next fix of each competitor to hand into the mark passing algorithm
        for (Entry<Competitor, Iterator<GPSFixMoving>> entry : fixesIterators.entrySet()) {
            if (entry.getValue().hasNext())
                nextFixes.put(entry.getKey(), entry.getValue().next());
        }

        while (!nextFixes.isEmpty()) {
            // Find the chronologically first unhandled fix.
            Entry<Competitor, GPSFixMoving> firstUnhandledFix = null;
            for (Entry<Competitor, GPSFixMoving> entry : nextFixes.entrySet()) {
                if (firstUnhandledFix == null
                        || entry.getValue().getTimePoint().compareTo(firstUnhandledFix.getValue().getTimePoint()) < 0) {
                    firstUnhandledFix = entry;
                }
            }
            if (firstUnhandledFix != null) {
                // compute a possible mark passing
                MarkPassing computedPassing = computeMarkPassings(firstUnhandledFix.getKey(),
                        firstUnhandledFix.getValue());
                // replace the fix we just handled by the next fix of the competitor's track
                if (fixesIterators.get(firstUnhandledFix.getKey()).hasNext()) {
                    nextFixes.put(firstUnhandledFix.getKey(), fixesIterators.get(firstUnhandledFix.getKey()).next());
                } else {
                    // If no further fixes can be obtained from the iterator, remove the competitor from the nextFixes
                    // map.
                    // This ensures the while loop eventually terminates after all fixes have been analyzed.
                    nextFixes.remove(firstUnhandledFix.getKey());
                }
                firstUnhandledFix = null;
                if (computedPassing != null) {
                    // A new mark passing has been detected.
                    // Find out if it overwrites a previous mark passing or is a new one.
                    MarkPassing latestPassingOfCompetitor = null;
                    int index = -1;
                    for (int i = 0; i < markPassings.size(); i++) {
                        MarkPassing p = markPassings.get(i);
                        if (p.getCompetitor().equals(computedPassing.getCompetitor())) {
                            latestPassingOfCompetitor = p;
                            index = i;
                        }
                    }

                    if (latestPassingOfCompetitor != null
                            && latestPassingOfCompetitor.getWaypoint().equals(computedPassing.getWaypoint())) {
                        // Latest passing of the competitor was at the same waypoint -> replace it with the new one
                        markPassings.set(index, computedPassing);
                    } else {
                        markPassings.add(computedPassing);
                    }
                }
            }
        }

        return markPassings;
    }

    /**
     * This method starts the actual mark passing detection algorithm. It is supposed to analyze if the given
     * {@link Competitor} passed a {@link Mark} at or before the given {@link GPSFixMoving}. If a {@link MarkPassing}
     * for a previously passed {@link Waypoint} is detected, it must only return a non-null result, if the returned
     * {@link MarkPassing} should overwrite the previously returned one.
     * 
     * @param competitor
     *            - the {@link Competitor} the given {@link GPSFixMoving} belongs to
     * @param fix
     *            - the {@link GPSFixMoving} representing the latest known position of the given {@link Competitor}
     * @return a {@link List} of mark passings
     */
    abstract MarkPassing computeMarkPassings(Competitor competitor, GPSFixMoving fix);

    /**
     * Compares the computed mark passings to the given ones. For each given sequence of mark passings, the one with the
     * timestamp closest to the given data is selected.
     * 
     * @param markPassings
     *            - a {@link Map} from each {@link Competitor} to a {@link Map} from each {@link Waypoint} to a
     *            {@link List} of mark passing sequences
     * @return how many mark passings were missed or detected way too early or too late
     */
    protected int compareCalculatedAndGivenPassings(List<MarkPassing> markPassings, boolean printDebug) {
        int overallMisses = 0;
        Iterable<Competitor> competitors = getRace().getCompetitors();
        for (Competitor c : competitors) {
            int misses = 0;
            NavigableSet<MarkPassing> givenPassingsForCompetitor = getTrackedRace().getMarkPassings(c);
            List<MarkPassing> calculatedPassingsForCompetitor = new ArrayList<MarkPassing>();
            for (MarkPassing p : markPassings) {
                if (p.getCompetitor().equals(c)) {
                    calculatedPassingsForCompetitor.add(p);
                }
            }
            if (printDebug)
                System.out.println("Competitor is " + c.getName());
            // Simultaneously iterate over given and computed mark passings and calculate the timedelta
            ListIterator<MarkPassing> calculatedPassingsIt = calculatedPassingsForCompetitor.listIterator();
            for (MarkPassing givenPassing : givenPassingsForCompetitor) {
                Waypoint passedWaypoint = givenPassing.getWaypoint();
                MarkPassing calculatedPassing;
                if (calculatedPassingsIt.hasNext()) {
                    calculatedPassing = calculatedPassingsIt.next();
                } else {
                    calculatedPassing = null;
                }

                if (calculatedPassing != null && calculatedPassing.getWaypoint().equals(passedWaypoint)) {
                    long timedelta = calculatedPassing.getTimePoint().asMillis()
                            - givenPassing.getTimePoint().asMillis();
                    String waypointType = isGate(passedWaypoint) ? "Gate" : "Buoy";
                    if (Math.abs(timedelta) < TIMEDELTA_TOLERANCE) {
                        // counts as detected
                        if (printDebug)
                            System.out.println("\tTimedelta of calculated mark passing for waypoint "
                                    + passedWaypoint.getName() + " (" + waypointType + "): " + timedelta + "ms, "
                                    + calculatedPassing.getTimePoint().asDate() + "(computed) vs. "
                                    + givenPassing.getTimePoint().asDate() + "(given)");
                    } else {
                        // timedelta too huge, counts as missed
                        misses++;
                        overallMisses++;
                        if (printDebug)
                            System.out.println("\tPassings of waypoint " + passedWaypoint.getName() + " ("
                                    + waypointType + ") are detected way off (" + timedelta + "ms, "
                                    + calculatedPassing.getTimePoint().asDate() + ")");
                    }
                } else {
                    // mark passing for given waypoint
                    misses++;
                    overallMisses++;
                    if (printDebug)
                        System.out.println("\tNo passings of waypoint " + passedWaypoint.getName());
                    if (calculatedPassingsIt.hasPrevious()) {
                        // rewind the iterator to the previous entry
                        calculatedPassingsIt.previous();
                    }
                }
            }
            if (printDebug)
                System.out.println("\t" + misses + " waypoints were missed.");
        }
        if (printDebug)
            System.out.println("Overall, " + overallMisses + " mark passings were missed.");
        return overallMisses;
    }

    /**
     * Get the previous {@link Waypoint} of a given {@link Waypoint}.
     * 
     * @param waypoint
     *            - the {@link Waypoint} to obtain the predecessor of
     * @return a {@link Waypoint} that has to be passed before the given {@link Waypoint} of the {@link Course}, or null
     *         if none
     */
    protected Waypoint getPreviousWaypoint(Waypoint waypoint) {
        Course course = getRace().getCourse();
        try {
            course.lockForRead();
            int i = course.getIndexOfWaypoint(waypoint);
            for (Waypoint w : course.getWaypoints()) {
                if (course.getIndexOfWaypoint(w) == i - 1) {
                    return w;
                }
            }
        } finally {
            course.unlockAfterRead();
        }
        return null;
    }

    /**
     * Get the next {@link Waypoint} after the given {@link Waypoint}.
     * 
     * @param waypoint
     *            - the {@link Waypoint} to obtain the successor of
     * @return a {@link Waypoint} that has to be passed after the given {@link Waypoint} of the {@link Course}, or null
     *         if none
     */
    protected Waypoint getNextWaypoint(Waypoint waypoint) {
        Course course = getRace().getCourse();
        try {
            course.lockForRead();
            int i = course.getIndexOfWaypoint(waypoint);
            for (Waypoint w : course.getWaypoints()) {
                if (course.getIndexOfWaypoint(w) == i + 1) {
                    return w;
                }
            }
        } finally {
            course.unlockAfterRead();
        }
        return null;
    }

    /**
     * Get the passing side of any given {@link Mark}. For single buoys this returns
     * {@link Waypoint#getPassingInstructions()}, for gates, the passing side of the {@link Waypoint}'s first
     * {@link Mark} is calculated and depending on the given {@link Mark} is returned unchanged (m = Mark 1) or inverted
     * (m = Mark 2). Since the calculation of the passing side uses the {@link DynamicGPSFixTrack} of the
     * {@link Waypoint}'s {@link Mark}s, a {@link TimePoint} has to be specified.
     * 
     * @param waypoint
     *            - the {@link Waypoint} to get the passing side of
     * @param m
     *            - the {@link Mark} of the {@link Waypoint} to get the passing side of
     * @param timePoint
     *            - the {@link TimePoint} to get the passing side at
     * @return the {@link NauticSide} the given {@link Mark} of the given {@link WayPoint} has to be passed on at the
     *         given {@link TimePoint}
     */
    protected PassingInstruction getPassingInstructionOfMark(Waypoint waypoint, Mark m, TimePoint timePoint) {
        if (isGate(waypoint)) {
            PassingInstruction mark1PassingInstruction = getPassingInstructionForMark1OfGate(waypoint, timePoint);
            if (m.equals(waypoint.getMarks().iterator().next())) {
                // m is mark 1 of gate
                return mark1PassingInstruction;
            } else if (mark1PassingInstruction.equals(PassingInstruction.Starboard)) {
                // m is mark 2 of gate -> opposite PassingInstruction of mark 1
                return PassingInstruction.Port;
            } else {
                return PassingInstruction.Starboard;
            }
        } else {
            // waypoint is not a gate
            return waypoint.getPassingInstructions();
        }
    }

    /**
     * Calculate the passing side of {@link Mark} 1 of the given {@link Waypoint} at the given {@link TimePoint}.
     * 
     * @param waypoint
     *            - the {@link Waypoint} to calculate the passing side for
     * @param timePoint
     *            - the {@link TimePoint} to calculate the passing side at
     * @return the {@link NauticSide} the {@link Mark} 1 of the given {@link Waypoint} has to be passed on
     */
    protected PassingInstruction getPassingInstructionForMark1OfGate(Waypoint waypoint, TimePoint timePoint) {
        if (!isGate(waypoint)) {
            return null;
        }
        // calculate the passing side of the first mark in the waypoints marks
        Position mark1Pos = null;
        Iterator<Mark> markIt = waypoint.getMarks().iterator();
        DynamicGPSFixTrack<Mark, GPSFix> mark1Track = getTrackedRace().getOrCreateTrack(markIt.next());
        try {
            mark1Track.lockForRead();
            mark1Pos = mark1Track.getFirstRawFixAtOrAfter(timePoint).getPosition();
        } finally {
            mark1Track.unlockAfterRead();
        }

        Position mark2Pos = null;
        DynamicGPSFixTrack<Mark, GPSFix> mark2Track = getTrackedRace().getOrCreateTrack(markIt.next());
        try {
            mark2Track.lockForRead();
            mark2Pos = mark2Track.getFirstRawFixAtOrAfter(timePoint).getPosition();
        } finally {
            mark2Track.unlockAfterRead();
        }

        if (getRace().getCourse().getFirstWaypoint().equals(waypoint)) {
            // first waypoint of the course, racing direction is towards next waypoint
            Position nextWpPos = getTrackedRace().getApproximatePosition(getNextWaypoint(waypoint), timePoint);
            Bearing mark1ToNextWp = mark1Pos.getBearingGreatCircle(nextWpPos);
            Bearing mark2ToNextWp = mark2Pos.getBearingGreatCircle(nextWpPos);
            if (mark1ToNextWp.getDifferenceTo(mark2ToNextWp).getDegrees() > 0) {
                return PassingInstruction.Starboard;
            } else {
                return PassingInstruction.Port;
            }
        } else {
            // racing direction is from previous waypoint
            Position prevWpPos = getTrackedRace().getApproximatePosition(getPreviousWaypoint(waypoint), timePoint);
            Bearing prevWpToMark1 = prevWpPos.getBearingGreatCircle(mark1Pos);
            Bearing prevWpToMark2 = prevWpPos.getBearingGreatCircle(mark2Pos);
            if (prevWpToMark1.getDifferenceTo(prevWpToMark2).getDegrees() > 0) {
                return PassingInstruction.Port;
            } else {
                return PassingInstruction.Starboard;
            }
        }
    }

    /**
     * Checks if a {@link Waypoint} has exactly 2 {@link Mark Marks}.
     * 
     * @param wp
     *            the {@link Waypoint} to check
     * @return <code>true</code> if {@link Waypoint#getMarks()} returns an {@link Iterable} with exactly 2 elements.
     */
    protected boolean isGate(Waypoint wp) {
        Iterator<Mark> it = wp.getMarks().iterator();
        if (it != null && it.hasNext()) {
            it.next();
            if (it.hasNext()) {
                it.next();
                if (!it.hasNext()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Calculates the passing {@link Bearing} of the given {@link Mark} which belongs to the given {@link Waypoint} at
     * the given {@link TimePoint}.
     * 
     * @param waypoint
     *            - the {@link Waypoint} the given {@link Mark} belongs to
     * @param mark
     *            - the {@link Mark} to calculate the passing {@link Bearing} for
     * @param time
     *            - the {@link TimePoint} at which to calculate the passing {@link Bearing}
     * @return a {@link Bearing} that represents the line a boat has to pass in order to round the given {@link Mark}
     */
    protected Bearing getPassingBearing(Waypoint waypoint, Mark mark, TimePoint time) {
        Course course = getRace().getCourse();
        Bearing passingBearing = null;

        Position markPos = null;
        DynamicGPSFixTrack<Mark, GPSFix> markTrack = getTrackedRace().getOrCreateTrack(mark);
        try {
            markTrack.lockForRead();
            markPos = markTrack.getFirstRawFixAtOrAfter(time).getPosition();
        } finally {
            markTrack.unlockAfterRead();
        }

        if (waypoint.equals(course.getFirstWaypoint()) || waypoint.equals(course.getLastWaypoint())) {
            // passing a line
            Mark otherMark = null;
            for (Mark m : waypoint.getMarks()) {
                if (!m.equals(mark)) {
                    otherMark = m;
                }
            }
            Position otherMarkPos = null;
            DynamicGPSFixTrack<Mark, GPSFix> otherMarkTrack = getTrackedRace().getOrCreateTrack(otherMark);
            try {
                otherMarkTrack.lockForRead();
                otherMarkPos = otherMarkTrack.getFirstRawFixAtOrAfter(time).getPosition();
            } finally {
                otherMarkTrack.unlockAfterRead();
            }
            passingBearing = markPos.getBearingGreatCircle(otherMarkPos);
        } else {
            // passing a single buoy

            Bearing bearingToNextWp = null;
            Bearing bearingfromPrevWp = null;
            Bearing bearingDiff = null;

            // calculate bearings to next and from previous waypoint
            if (!course.getLastWaypoint().equals(waypoint)) {
                // not last waypoint of the race
                Waypoint nextWp = getNextWaypoint(waypoint);
                Position nextWaypointPos = getTrackedRace().getApproximatePosition(nextWp, time);

                bearingToNextWp = markPos.getBearingGreatCircle(nextWaypointPos);
            }
            if (!course.getFirstWaypoint().equals(waypoint)) {
                // not first waypoint of the course
                Waypoint prevWp = getPreviousWaypoint(waypoint);
                Position prevWaypointPos = getTrackedRace().getApproximatePosition(prevWp, time);

                bearingfromPrevWp = prevWaypointPos.getBearingGreatCircle(markPos);
            }

            // depending on passing side, set the bearing difference
            PassingInstruction passingInstruction = getPassingInstructionOfMark(waypoint, mark, time);
            if (passingInstruction != null && passingInstruction.equals(PassingInstruction.Starboard)) {
                bearingDiff = new DegreeBearingImpl(-90);
            } else {
                bearingDiff = new DegreeBearingImpl(90);
            }

            // calculate the passing bearing
            if (course.getFirstWaypoint().equals(waypoint)) {
                passingBearing = bearingToNextWp.add(bearingDiff);
            } else if (course.getLastWaypoint().equals(waypoint)) {
                passingBearing = bearingfromPrevWp.add(bearingDiff);
            } else {
                passingBearing = bearingToNextWp.add(bearingDiff).middle(bearingfromPrevWp.add(bearingDiff));
            }
        }
        return passingBearing;
    }

    /**
     * Finds the {@link Entry} with the best {@link MarkPassing} of the key set. A {@link MarkPassing} is better than
     * another if:
     * <ol>
     * <li>it is on the correct side of the mark and the other one is not</li>
     * <li>it is closer to the mark than the other one</li>
     * <li>its TimePoint is smaller (it happened earlier) than the other one</li>
     * </ol>
     * 
     * @param possiblePassings
     * @return
     */
    protected Entry<MarkPassing, Position> findBestMarkPassing(Map<MarkPassing, Position> passings) {
        if (passings.size() == 1) {
            return passings.entrySet().iterator().next();
        } else if (passings.isEmpty()) {
            return null;
        }
        Map<MarkPassing, Position> possibleResults = new HashMap<MarkPassing, Position>();
        // check if mark was passed on correct side
        for (Entry<MarkPassing, Position> entry : passings.entrySet()) {
            // find out on which side of the mark was passed
            MarkPassing passing = entry.getKey();
            Position passPos = entry.getValue();
            Position markPos = null;
            DynamicGPSFixTrack<Mark, GPSFix> markTrack = getTrackedRace().getOrCreateTrack(
                    passing.getWaypoint().getMarks().iterator().next());
            try {
                markTrack.lockForRead();
                markPos = markTrack.getLastFixAtOrBefore(passing.getTimePoint()).getPosition();
            } finally {
                markTrack.unlockAfterRead();
            }
            Bearing bearingDiff = markPos.getBearingGreatCircle(passPos).getDifferenceTo(
                    getPassingBearing(passing.getWaypoint(), passing.getWaypoint().getMarks().iterator().next(),
                            passing.getTimePoint()));
            if (Math.abs(bearingDiff.getDegrees()) < 90) {
                // passed on correct side of mark
                possibleResults.put(entry.getKey(), entry.getValue());
            }
        }
        if (possibleResults.size() > 0) {
            // All possible passings are on correct side, pass them to the distance test.
            // If all passings were on the wrong side, passings is not overwritten and they are still passed to the
            // distance test.
            passings.putAll(possibleResults);
        }
        possibleResults.clear();

        // find the mark passing with the smallest distance to the mark
        for (Entry<MarkPassing, Position> entry : passings.entrySet()) {
            // find out how far away the mark was passed
            MarkPassing passing = entry.getKey();
            Position passPos = entry.getValue();
            Position markPos = null;
            DynamicGPSFixTrack<Mark, GPSFix> markTrack = getTrackedRace().getOrCreateTrack(
                    passing.getWaypoint().getMarks().iterator().next());
            try {
                markTrack.lockForRead();
                markPos = markTrack.getLastFixAtOrBefore(passing.getTimePoint()).getPosition();
            } finally {
                markTrack.unlockAfterRead();
            }
            if (possibleResults.isEmpty()) {
                possibleResults.put(passing, passPos);
            } else {
                Distance shortestDistance = markPos
                        .getDistance(possibleResults.entrySet().iterator().next().getValue());
                Distance newDistance = markPos.getDistance(passPos);
                if (shortestDistance.compareTo(newDistance) == 0) {
                    // same distance, add to possible results
                    possibleResults.put(passing, passPos);
                } else if (shortestDistance.compareTo(newDistance) > 0) {
                    // shorter distance, remove all other possible results and add this one
                    possibleResults.clear();
                    possibleResults.put(passing, passPos);
                }
            }
        }
        if (possibleResults.size() == 1) {
            // only one possible result left, we're done
            return possibleResults.entrySet().iterator().next();
        } else if (possibleResults.size() > 1) {
            // all possible passings have the same distance to the mark, pass them to the timing test
            passings.putAll(possibleResults);
        }
        possibleResults.clear();

        // find the mark passing with the smallest timestamp
        for (Entry<MarkPassing, Position> entry : passings.entrySet()) {
            // find out how far away the mark was passed
            MarkPassing passing = entry.getKey();
            Position passPos = entry.getValue();
            if (possibleResults.isEmpty()) {
                possibleResults.put(passing, passPos);
            } else {
                TimePoint smallestTime = possibleResults.entrySet().iterator().next().getKey().getTimePoint();
                TimePoint newTime = passing.getTimePoint();
                if (smallestTime.compareTo(newTime) == 0) {
                    // same timestamp, add to possible results
                    possibleResults.put(passing, passPos);
                } else if (smallestTime.compareTo(newTime) > 0) {
                    // smaller timestamp, remove all other possible results and add this one
                    possibleResults.clear();
                    possibleResults.put(passing, passPos);
                }
            }
        }
        if (possibleResults.size() > 0) {
            // all possible passings have the same timestamp, just return the first one
            return possibleResults.entrySet().iterator().next();
        }

        return null;
    }

}