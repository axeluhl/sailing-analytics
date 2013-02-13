package com.sap.sailing.domain.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.NauticSide;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.domain.tractracadapter.ReceiverType;

/**
 * Tests the mark passing algorithm by using the data provided by an {@link OnlineTracTracBasedTest}.
 * 
 * @author Martin Hanysz
 *
 */
public class MarkPassingTest extends OnlineTracTracBasedTest {

	/**
	 * Controls the size of the hot zone around buoys. It is multiplied with the boat length.
	 */
	private static final double HOT_ZONE_TO_BOAT_LENGHT_RATIO = 10.0;
	
	/**
	 * Controls how long a new mark passing will overwrite the previous one for the same mark.
	 * If a new mark passing is detected for the same waypoint within MARK_PASSING_CORRECTION_BOUNDARY milliseconds, the previous one is overwritten.
	 */
	private static final long MARK_PASSING_CORRECTION_BOUNDARY = 100000;
	private boolean forceReload = false; // set to true to reload race data

	public MarkPassingTest() throws MalformedURLException, URISyntaxException {
		super();
	}
	
	@Before
	public void setUp() throws IOException, InterruptedException, URISyntaxException {
		super.setUp();
		String raceID = "357c700a-9d9a-11e0-85be-406186cbf87c";
		if (!loadData(raceID) && !forceReload) {
			System.out.println("Downloading new data from the web.");
		    super.setUp(	"event_20110609_KielerWoch",
		    				/* raceId */raceID, 
		    				new ReceiverType[] {	ReceiverType.MARKPASSINGS,
		    										ReceiverType.MARKPOSITIONS,
		            								ReceiverType.RACECOURSE, 
		            								ReceiverType.RAWPOSITIONS });
		    OnlineTracTracBasedTest.fixApproximateMarkPositionsForWindReadOut(getTrackedRace(), new MillisecondsTimePoint(
		            new GregorianCalendar(2011, 05, 23).getTime()));
		    getTrackedRace().recordWind(
		            new WindImpl(/* position */null, MillisecondsTimePoint.now(), new KnotSpeedWithBearingImpl(12,
		                    new DegreeBearingImpl(65))), new WindSourceImpl(WindSourceType.WEB));
		    saveData();
		}
	}
	
	/**
	 * Loads stored data for the given raceID or returns false if no data is present.
	 * @param raceID - ID of the race to load from disk
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

		if (obj != null && obj instanceof DynamicTrackedRace) {
			setTrackedRace((DynamicTrackedRace) obj);
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

	@Test
	public void testMarkPassings() {
		Iterable<Competitor> competitors = getRace().getCompetitors();
		Course course = getRace().getCourse();

		assertNotNull(competitors);
		assertTrue(getTrackedRace().hasGPSData());
		
		Map<Competitor, Map<Waypoint, List<List<MarkPassing>>>> markPassings = new HashMap<>();
		
		// compute MarkPassings for all competitors
		for (Competitor competitor : competitors) {
			DynamicGPSFixTrack<Competitor, GPSFixMoving> track = getTrackedRace().getTrack(competitor);
			markPassings.put(competitor, new HashMap<Waypoint, List<List<MarkPassing>>>());
			try {
				track.lockForRead();
				// for each GPS fix, check which waypoints are passed
				for (GPSFixMoving fix : track.getFixes()) {
					for (Waypoint nextWaypointToPass : course.getWaypoints()) {
						MarkPassing passing = null;
						if (isGate(nextWaypointToPass)) {
							if (	nextWaypointToPass.equals(course.getFirstWaypoint()) || 
									nextWaypointToPass.equals(course.getLastWaypoint())) {
								// start or finish line
								passing = getLinePassing(nextWaypointToPass, fix, competitor);
							} else {
								// gate
								//passing = getBuoyPassing(nextWaypointToPass, fix, competitor);
								passing = getLinePassing(nextWaypointToPass, fix, competitor);
							}
						} else {
							// waypoint is single buoy
							passing = getBuoyPassing(nextWaypointToPass, fix, competitor);
						}
						
						if (passing != null) {
							// detected a passing
							Map<Waypoint, List<List<MarkPassing>>> passingsForCompetitor = markPassings.get(competitor);
							// get previous passing sequences for the passed waypoint
							if (!passingsForCompetitor.containsKey(passing.getWaypoint())) {
								passingsForCompetitor.put(passing.getWaypoint(), new ArrayList<List<MarkPassing>>());
							}
							List<List<MarkPassing>> passingsOfWaypoint = passingsForCompetitor.get(passing.getWaypoint());
							if (passingsOfWaypoint.isEmpty()) {
								// no previous passings for this waypoint
								List<MarkPassing> newSequence = new ArrayList<MarkPassing>();
								newSequence.add(passing);
								passingsOfWaypoint.add(newSequence);
							} else {
								// previous passings for this waypoint exist -> check if this one is a new sequence or belongs to an existing one
								// belong to a sequence <=> currentPassing happened at max MARK_PASSING_CORRECTION_BOUNDARY ms after last mark passing of same waypoint
								List<MarkPassing> lastSequence = passingsOfWaypoint.get(passingsOfWaypoint.size()-1);
								if (passing.getTimePoint().asMillis() - lastSequence.get(lastSequence.size()-1).getTimePoint().asMillis() < MARK_PASSING_CORRECTION_BOUNDARY) {
									// belongs to same sequence
									lastSequence.add(passing);
								} else {
									// is start of a new sequence
									List<MarkPassing> newSequence = new ArrayList<MarkPassing>();
									newSequence.add(passing);
									passingsOfWaypoint.add(newSequence);
								}
							}
						}
					}
				}
			} finally {
				track.unlockAfterRead();
			}
		}
		
		// compare computed mark passings to given ones
		int overallMisses = compareCalculatedAndGivenPassings(markPassings);
		assertTrue("Calculation returned less mark passings than the test data contains", overallMisses == 0);
	}

	private int compareCalculatedAndGivenPassings(Map<Competitor, Map<Waypoint, List<List<MarkPassing>>>> markPassings) {
		int overallMisses = 0;
		Iterable<Competitor> competitors = getRace().getCompetitors();
		for (Competitor c : competitors) {
			int misses = 0;
			NavigableSet<MarkPassing> givenPassings = getTrackedRace().getMarkPassings(c);
			Map<Waypoint, List<List<MarkPassing>>> calculatedPassings = markPassings.get(c);
			System.out.println("Competitor is " + c.getName());
			// for all given mark passings, find the corresponding detected passing sequences
			for (MarkPassing givenPassing : givenPassings) {			
				Waypoint passedWaypoint = givenPassing.getWaypoint();
				if (calculatedPassings.containsKey(passedWaypoint)) {
					// waypoint passings were detected, find the mark passing with the timePoint closest to the given passing
					List<List<MarkPassing>> waypointSequences = calculatedPassings.get(passedWaypoint);
					MarkPassing closestPassing = null;
					for (List<MarkPassing> sequence : waypointSequences) {
						for (MarkPassing passing : sequence) {
							if (	closestPassing == null ||
									Math.abs(givenPassing.getTimePoint().asMillis() - closestPassing.getTimePoint().asMillis()) >
									Math.abs(givenPassing.getTimePoint().asMillis() - passing.getTimePoint().asMillis())) {
								// new closest passing
								closestPassing = passing;								
							}
						}
					}
					long timedelta = closestPassing.getTimePoint().asMillis() - givenPassing.getTimePoint().asMillis();
					String waypointType = isGate(passedWaypoint) ? "Gate" : "Buoy";
					if (Math.abs(timedelta) < 10000) {
						// counts as detected
						System.out.println("\tTimedelta for closest detected passing for Mark " + passedWaypoint.getName() + " (" + waypointType + "): " + timedelta + "ms, " + closestPassing.getTimePoint().asDate() + "(computed) vs. " + givenPassing.getTimePoint().asDate() + "(given)");
					} else {
						// timedelta too huge, counts as missed
						misses++;
						overallMisses++;
						System.out.println("\tPassings of waypoint " + passedWaypoint.getName() + " (" + waypointType + ") are detected way off (" + timedelta + "ms, " + closestPassing.getTimePoint().asDate() + ")");
					}
				} else {
					// no sequences for given waypoint at all
					misses++;
					overallMisses++;
					System.out.println("\tNo passings of waypoint " + passedWaypoint.getName());
				}
			}
			System.out.println("\t" + misses + " waypoints were missed.");
		}
		System.out.println("Overall, " + overallMisses + " mark passings were missed.");
		return overallMisses;
	}

	private MarkPassing getBuoyPassing(Waypoint waypoint, GPSFixMoving fix, Competitor competitor) {
		if (waypoint == null || getTrackedRace().getStartOfRace().compareTo(fix.getTimePoint()) > 0) {
			// no waypoint or race not started yet
			return null;
		}

		for (Mark mark : waypoint.getMarks()) {
			// get position of given mark
			Position markPos = null;
			DynamicGPSFixTrack<Mark, GPSFix> markTrack = getTrackedRace().getOrCreateTrack(mark);
			try {
				markTrack.lockForRead();
				markPos = markTrack.getFirstRawFixAtOrAfter(fix.getTimePoint()).getPosition();
			} finally {
				markTrack.unlockAfterRead();
			}
	
			// check if boat is in "hot zone"
			// boat in "hot zone" <=> distance from boat to mark < boat length * x
			if (fix.getPosition().getDistance(markPos).compareTo(competitor.getBoat().getBoatClass().getHullLength().scale(HOT_ZONE_TO_BOAT_LENGHT_RATIO)) > 0) {
				// not in "hot zone"
				break;
			}
			
			// calculate mark passing bearing for waypoint
			Course course = getRace().getCourse();
			Bearing markToBoatBearing = markPos.getBearingGreatCircle(fix.getPosition());
			Bearing bearingToNextWp = null;
			Bearing bearingfromPrevWp = null;
			Bearing bearingDiff = null;
			Bearing passingBearing = null;					
			
			// calculate bearings to next and from previous waypoint
			if (!course.getLastWaypoint().equals(waypoint)) {
				// not last waypoint of the race
				Waypoint nextWp = getNextWaypoint(waypoint);
				Position nextWaypointPos = getTrackedRace().getApproximatePosition(nextWp, fix.getTimePoint());
				
				bearingToNextWp = markPos.getBearingGreatCircle(nextWaypointPos);
			}
			if (!course.getFirstWaypoint().equals(waypoint)) {
				// not first waypoint of the course
				Waypoint prevWp = getPreviousWaypoint(waypoint);
				Position prevWaypointPos = getTrackedRace().getApproximatePosition(prevWp, fix.getTimePoint());
				
				bearingfromPrevWp = prevWaypointPos.getBearingGreatCircle(markPos);
			}
			
			// depending on passing side, set the bearing difference
			NauticSide passingSide = getPassingSideOfMark(waypoint, mark, fix.getTimePoint());
			if (passingSide != null && passingSide.equals(NauticSide.STARBOARD)) {	
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
			
			double passingBearingDelta = markToBoatBearing.getDifferenceTo(passingBearing).getDegrees();
			if (	bearingDiff.getDegrees() > 0 &&
					passingBearingDelta > 0) {
				// markToBoatBearing is smaller than passing bearing -> passed on port
				return getDomainFactory().createMarkPassing(fix.getTimePoint(), waypoint, competitor);
			} else if (	bearingDiff.getDegrees() < 0 &&
						passingBearingDelta < 0) {
				// markToBoatBearing is greater than passing bearing -> passed on stb
				return getDomainFactory().createMarkPassing(fix.getTimePoint(), waypoint, competitor);
			}
		}
		return null;
	}

	private Waypoint getPreviousWaypoint(Waypoint waypoint) {
		Course course = getRace().getCourse();
		int i = course.getIndexOfWaypoint(waypoint);
		for (Waypoint w : course.getWaypoints()) {
			if (course.getIndexOfWaypoint(w) == i-1) {
				return w;
			}
		}
		return null;
	}

	private Waypoint getNextWaypoint(Waypoint waypoint) {
		Course course = getRace().getCourse();
		int i = course.getIndexOfWaypoint(waypoint);
		for (Waypoint w : course.getWaypoints()) {
			if (course.getIndexOfWaypoint(w) == i+1) {
				return w;
			}
		}
		return null;
	}

	private MarkPassing getLinePassing(Waypoint waypoint, GPSFixMoving fix, Competitor competitor) {
		if (waypoint == null || !isGate(waypoint)) {
			// no waypoint, waypoint not a gate, or race not started yet
			return null;
		}
		
		/* A boat passes the line, iff the bearing from the boat to mark 1 of the gate equals 
		 * the bearing from mark 2 of the gate to mark 1 of the gate AND the bearing from the boat
		 * to mark 2 of the gate equals the bearing from mark 1 of the gate to mark 2 of the gate.
		 */
		// get tracks of both marks
		Iterator<Mark> markIt = waypoint.getMarks().iterator();
		DynamicGPSFixTrack<Mark, GPSFix> mark1Track = null;
		DynamicGPSFixTrack<Mark, GPSFix> mark2Track = null;
		if (markIt.hasNext()) {
			mark1Track = getTrackedRace().getOrCreateTrack(markIt.next());
			if (markIt.hasNext())
				mark2Track = getTrackedRace().getOrCreateTrack(markIt.next());
		}
		
		// get positions for both marks of the gate
		Position mark1Pos = null;
		Position mark2Pos = null;
		try {
			mark1Track.lockForRead();
			try {
				mark2Track.lockForRead();
				mark1Pos = mark1Track.getFirstRawFixAtOrAfter(fix.getTimePoint()).getPosition();
				mark2Pos = mark2Track.getFirstRawFixAtOrAfter(fix.getTimePoint()).getPosition();
			} finally {
				mark2Track.unlockAfterRead();				
			}
		} finally {
			mark1Track.unlockAfterRead();
		}
		
		// calculate if boat is in "hot zone" of gate
		// in "hot zone" <=> distance from any mark of gate to boat < length of line between marks
		Distance lineLength = mark1Pos.getDistance(mark2Pos);
		if (	mark1Pos.getDistance(fix.getPosition()).compareTo(lineLength) > 0 && 
				mark2Pos.getDistance(fix.getPosition()).compareTo(lineLength) > 0) {
			// not in "hot zone"
			return null;
		}
		
		// calculate bearings from boat to 1st and 2nd mark, from 1st mark to 2nd mark, and from 2nd mark to 1st mark
		Bearing boatToMark1Bearing = fix.getPosition().getBearingGreatCircle(mark1Pos);
		Bearing mark1ToMark2Bearing = mark1Pos.getBearingGreatCircle(mark2Pos);
		Bearing boatToMark2Bearing = fix.getPosition().getBearingGreatCircle(mark2Pos);
		Bearing mark2ToMark1Bearing = mark2Pos.getBearingGreatCircle(mark1Pos);
		
		NauticSide passingSideOfMark1OfGate = getPassingSideForMark1OfGate(waypoint, fix.getTimePoint());
		double mark1PassingBearingDelta = boatToMark1Bearing.getDifferenceTo(mark2ToMark1Bearing).getDegrees();
		double mark2PassingBearingDelta = boatToMark2Bearing.getDifferenceTo(mark1ToMark2Bearing).getDegrees();
		
		if (	passingSideOfMark1OfGate.equals(NauticSide.STARBOARD) &&
				mark1PassingBearingDelta < 0 &&				
				mark2PassingBearingDelta > 0) {
			// gate passed if mark1 had to be passed on stb
			return getDomainFactory().createMarkPassing(fix.getTimePoint(), waypoint, competitor);
		} else if (	passingSideOfMark1OfGate.equals(NauticSide.PORT) &&
				mark1PassingBearingDelta > 0 &&				
				mark2PassingBearingDelta < 0) {
			// gate passed if mark1 had to be passed on port
			return getDomainFactory().createMarkPassing(fix.getTimePoint(), waypoint, competitor);
		}
		return null;
	}

	private NauticSide getPassingSideOfMark(Waypoint waypoint, Mark m, TimePoint timePoint) {
		if (isGate(waypoint)) {
			NauticSide mark1PassingSide = getPassingSideForMark1OfGate(waypoint, timePoint);
			if (m.equals(waypoint.getMarks().iterator().next())) {
				// m is mark 1 of gate
				return mark1PassingSide;
			} else if (mark1PassingSide.equals(NauticSide.STARBOARD)){
				// m is mark 2 of gate -> opposite passingSide of mark 1
				return NauticSide.PORT;
			} else {
				return NauticSide.STARBOARD;
			}
		} else {
			// waypoint is not a gate
			return waypoint.getPassingSide();
		}
	}
	
	private NauticSide getPassingSideForMark1OfGate(Waypoint waypoint, TimePoint timePoint) {
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
				return NauticSide.STARBOARD;
			} else {
				return NauticSide.PORT;
			}
		} else {
			// racing direction is from previous waypoint
			Position prevWpPos = getTrackedRace().getApproximatePosition(getPreviousWaypoint(waypoint), timePoint);
			Bearing prevWpToMark1 = prevWpPos.getBearingGreatCircle(mark1Pos);
			Bearing prevWpToMark2 = prevWpPos.getBearingGreatCircle(mark2Pos);
			if (prevWpToMark1.getDifferenceTo(prevWpToMark2).getDegrees() > 0) {
				return NauticSide.PORT;
			} else {
				return NauticSide.STARBOARD;
			}
		}
	}

	/**
	 * Checks if a {@link Waypoint} has exactly 2 {@link Mark Marks}.
	 * @param wp the {@link Waypoint} to check
	 * @return <code>true</code> if {@link Waypoint#getMarks()} returns an {@link Iterable} with exactly 2 elements.
	 */
	private boolean isGate(Waypoint wp) {
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

}
