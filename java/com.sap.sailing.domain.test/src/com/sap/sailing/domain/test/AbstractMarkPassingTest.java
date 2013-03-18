package com.sap.sailing.domain.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.GregorianCalendar;
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
import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.domain.tractracadapter.ReceiverType;

/**
 * Tests the mark passing algorithm by using the data provided by an {@link OnlineTracTracBasedTest}.
 * Subclasses may implement different mark passing detection algorithms.
 * 
 * @author Martin Hanysz
 *
 */
public abstract class AbstractMarkPassingTest extends OnlineTracTracBasedTest {

	/**
	 * Reload the test data from the web?
	 */
	private boolean forceReload = false;

	public AbstractMarkPassingTest() throws MalformedURLException,
			URISyntaxException {
		super();
	}

	@Before
	public void setUp() throws IOException, InterruptedException,
			URISyntaxException {
				super.setUp();
				String raceID = "357c700a-9d9a-11e0-85be-406186cbf87c";
				if (!loadData(raceID) && !forceReload) {
					System.out.println("Downloading new data from the web.");
				    this.setUp("event_20110609_KielerWoch",
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
		Map<Competitor, Map<Waypoint, List<List<MarkPassing>>>> markPassings = computeMarkPassings();
		// compare computed mark passings to given ones
		int overallMisses = compareCalculatedAndGivenPassings(markPassings);
		assertTrue("Calculation returned less mark passings than the test data contains", overallMisses == 0);
	}

	/**
	 * This method starts the actual mark passing detection algorithm.
	 * For every competitor, sequences of mark passings for each waypoint of the course can be calculated.
	 * One sequence may contain multiple possible mark passings for the same waypoint that may represent the same passing of that specific waypoint.
	 * The list of sequences returned in the result should contain one sequence for each time the waypoint has to be passed to complete the course.
	 * @return a {@link Map} from each {@link Competitor} to a {@link Map} from each {@link Waypoint} to a {@link List} of mark passing sequences  
	 */
	abstract Map<Competitor, Map<Waypoint, List<List<MarkPassing>>>> computeMarkPassings();

	/**
	 * Compares the computed mark passings to the given ones.
	 * For each given sequence of mark passings, the one with the timestamp closest to the given data is selected.
	 * @param markPassings - a {@link Map} from each {@link Competitor} to a {@link Map} from each {@link Waypoint} to a {@link List} of mark passing sequences
	 * @return how many mark passings were missed or detected way too early or too late
	 */
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

	/**
	 * Get the previous {@link Waypoint} of a given {@link Waypoint}. 
	 * @param waypoint - the {@link Waypoint} to obtain the predecessor of
	 * @return a {@link Waypoint} that has to be passed before the given {@link Waypoint} of the {@link Course}
	 */
	protected Waypoint getPreviousWaypoint(Waypoint waypoint) {
		Course course = getRace().getCourse();
		int i = course.getIndexOfWaypoint(waypoint);
		for (Waypoint w : course.getWaypoints()) {
			if (course.getIndexOfWaypoint(w) == i-1) {
				return w;
			}
		}
		return null;
	}

	/**
	 * Get the next {@link Waypoint} after the given {@link Waypoint}.
	 * @param waypoint - the {@link Waypoint} to obtain the successor of
	 * @return a {@link Waypoint} that has to be passed after the given {@link Waypoint} of the {@link Course}
	 */
	protected Waypoint getNextWaypoint(Waypoint waypoint) {
		Course course = getRace().getCourse();
		int i = course.getIndexOfWaypoint(waypoint);
		for (Waypoint w : course.getWaypoints()) {
			if (course.getIndexOfWaypoint(w) == i+1) {
				return w;
			}
		}
		return null;
	}

	/**
	 * Get the passing side of any given {@link Mark}.
	 * For single buoys this returns {@link Waypoint#getPassingSide()}, for gates, the passing side of the {@link Waypoint}'s first {@link Mark} is calculated and depending on the given {@link Mark} is returned unchanged (m = Mark 1) or inverted (m = Mark 2).
	 * Since the calculation of the passing side uses the {@link DynamicGPSFixTrack} of the {@link Waypoint}'s {@link Mark}s, a {@link TimePoint} has to be specified.
	 * @param waypoint - the {@link Waypoint} to get the passing side of
	 * @param m - the {@link Mark} of the {@link Waypoint} to get the passing side of
	 * @param timePoint - the {@link TimePoint} to get the passing side at
	 * @return the {@link NauticSide} the given {@link Mark} of the given {@link Waypoint} has to be passed on at the given {@link TimePoint}
	 */
	protected NauticalSide getPassingSideOfMark(Waypoint waypoint, Mark m, TimePoint timePoint) {
		if (isGate(waypoint)) {
			NauticalSide mark1PassingSide = getPassingSideForMark1OfGate(waypoint, timePoint);
			if (m.equals(waypoint.getMarks().iterator().next())) {
				// m is mark 1 of gate
				return mark1PassingSide;
			} else if (mark1PassingSide.equals(NauticalSide.STARBOARD)){
				// m is mark 2 of gate -> opposite passingSide of mark 1
				return NauticalSide.PORT;
			} else {
				return NauticalSide.STARBOARD;
			}
		} else {
			// waypoint is not a gate
			return waypoint.getPassingSide();
		}
	}

	/**
	 * Calculate the passing side of {@link Mark} 1 of the given {@link Waypoint} at the given {@link TimePoint}.
	 * @param waypoint - the {@link Waypoint} to calculate the passing side for
	 * @param timePoint - the {@link TimePoint} to calculate the passing side at
	 * @return the {@link NauticSide} the {@link Mark} 1 of the given {@link Waypoint} has to be passed on
	 */
	protected NauticalSide getPassingSideForMark1OfGate(Waypoint waypoint, TimePoint timePoint) {
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
				return NauticalSide.STARBOARD;
			} else {
				return NauticalSide.PORT;
			}
		} else {
			// racing direction is from previous waypoint
			Position prevWpPos = getTrackedRace().getApproximatePosition(getPreviousWaypoint(waypoint), timePoint);
			Bearing prevWpToMark1 = prevWpPos.getBearingGreatCircle(mark1Pos);
			Bearing prevWpToMark2 = prevWpPos.getBearingGreatCircle(mark2Pos);
			if (prevWpToMark1.getDifferenceTo(prevWpToMark2).getDegrees() > 0) {
				return NauticalSide.PORT;
			} else {
				return NauticalSide.STARBOARD;
			}
		}
	}

	/**
	 * Checks if a {@link Waypoint} has exactly 2 {@link Mark Marks}.
	 * @param wp the {@link Waypoint} to check
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

}