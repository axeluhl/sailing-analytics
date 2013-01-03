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
import java.util.Map.Entry;
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

	private boolean forceReload = false; // set to true to reload race data

	public MarkPassingTest() throws MalformedURLException, URISyntaxException {
		super();
	}
	
	@Before
	public void setUp() throws IOException, InterruptedException, URISyntaxException {
		super.setUp();
		String raceID = "357c700a-9d9a-11e0-85be-406186cbf87c";
		if (!loadData(raceID) && !forceReload) {
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
			fs = new FileInputStream(path);
			os = new ObjectInputStream(fs);
			obj = os.readObject();
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (fs != null) {
				try {
					fs.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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

		assertNotNull(competitors);
		assertTrue(getTrackedRace().hasGPSData());
		
		Map<Competitor, List<MarkPassing>> markPassings = new HashMap<>();
		
		// compute MarkPassings for all competitors
		for (Competitor competitor : competitors) {
			markPassings.put(competitor, new ArrayList<MarkPassing>());
			Iterator<Waypoint> waypointsIt = getRace().getCourse().getWaypoints().iterator();
			
			assertNotNull(waypointsIt);
			assertTrue(waypointsIt.hasNext());
			
			DynamicGPSFixTrack<Competitor, GPSFixMoving> track = getTrackedRace().getTrack(competitor);
			Waypoint nextWaypointToPass = waypointsIt.next();
			track.lockForRead();
			for (GPSFixMoving fix : track.getFixes()) {
				MarkPassing passing = null;
				if (isGate(nextWaypointToPass)) {
					passing = getGatePassing(nextWaypointToPass, fix, competitor);
				} else {
					passing = getBuoyPassing(nextWaypointToPass, fix, competitor);
				}
				if (passing != null) {
					markPassings.get(competitor).add(passing);
					if (waypointsIt.hasNext()) {
						nextWaypointToPass = waypointsIt.next(); 
					} else {
						// passed all waypoints of the course
						break;
					}
				}
			}
			track.unlockAfterRead();
		}
		
		// compare computed mark passings to given ones
		for (Entry<Competitor, List<MarkPassing>> e : markPassings.entrySet()) {
			NavigableSet<MarkPassing> givenPassings = getTrackedRace().getMarkPassings((Competitor) e.getKey());
			List<MarkPassing> computedPassings = (List<MarkPassing>) e.getValue();
			Iterator<MarkPassing> givenIt = givenPassings.iterator();
			long averagePerCompetitorTimeDelta = 0;
			for (MarkPassing computedPassing : computedPassings) {
				MarkPassing givenPassing = null;
				if (givenIt.hasNext()) {
					givenPassing = givenIt.next();
				}
				
				long timeDelta = givenPassing.getTimePoint().asMillis() - computedPassing.getTimePoint().asMillis();
				System.out.println("TimeDelta is " + timeDelta + "ms at waypoint " + computedPassing.getWaypoint().getName());

				averagePerCompetitorTimeDelta += timeDelta;
			}
			if (computedPassings.size() != 0)
				averagePerCompetitorTimeDelta /= computedPassings.size();
			System.out.println("Average TimeDelta for Competitor " + e.getKey().getName() + " is " + averagePerCompetitorTimeDelta + "ms");
			//assertTrue(averagePerCompetitorTimeDelta < 10000);
		}
	}

	private MarkPassing getBuoyPassing(Waypoint waypoint, GPSFixMoving fix, Competitor competitor) {
		if (waypoint == null) {
			return null;
		}
		// calculate mark passing bearing for waypoint
		Course course = getRace().getCourse();
		Position waypointPos = getTrackedRace().getApproximatePosition(waypoint, fix.getTimePoint());
		Bearing markToBoatBearing = waypointPos.getBearingGreatCircle(fix.getPosition());
		Bearing bearingToNextWp = null;
		Bearing bearingfromPrevWp = null;
		Bearing bearingDiff = null;
		Bearing passingBearing = null;
				
		// calculate bearings to next and from previous waypoint
		if (!course.getLastWaypoint().equals(waypoint)) {
			// not last waypoint of the race
			Waypoint nextWp = getNextWaypoint(waypoint);
			Position nextWaypointPos = getTrackedRace().getApproximatePosition(nextWp, fix.getTimePoint());
			
			bearingToNextWp = waypointPos.getBearingGreatCircle(nextWaypointPos);
		}
		if (!course.getFirstWaypoint().equals(waypoint)) {
			// not first waypoint of the course
			Waypoint prevWp = getPreviousWaypoint(waypoint);
			Position prevWaypointPos = getTrackedRace().getApproximatePosition(prevWp, fix.getTimePoint());
			
			bearingfromPrevWp = prevWaypointPos.getBearingGreatCircle(waypointPos);
		}
		
		// depending on passing side, set the bearing difference
		if (waypoint.getPassingSide() != null && waypoint.getPassingSide().equals(NauticSide.STARBOARD)) {
			bearingDiff = new DegreeBearingImpl(90);
		} else {
			bearingDiff = new DegreeBearingImpl(-90);
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
				passingBearingDelta > 0 &&
				passingBearingDelta < 90) {
			// markToBoatBearing is smaller than passing bearing -> passed on stb
			return getDomainFactory().createMarkPassing(fix.getTimePoint(), waypoint, competitor);
		} else if (	bearingDiff.getDegrees() < 0 &&
					passingBearingDelta < 0 &&
					passingBearingDelta > -90) {
			// markToBoatBearing is greater than passing bearing -> passed on port
			return getDomainFactory().createMarkPassing(fix.getTimePoint(), waypoint, competitor);
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

	private MarkPassing getGatePassing(Waypoint waypoint, GPSFixMoving fix, Competitor competitor) {
		if (waypoint == null) {
			return null;
		}
		// competitor passes gate, iff bearing from competitor to first mark equals bearing from second mark to first mark
		// get tracks of both marks
		Iterator<Mark> markIt = waypoint.getMarks().iterator();
		DynamicGPSFixTrack<Mark, GPSFix> mark1Track = null;
		DynamicGPSFixTrack<Mark, GPSFix> mark2Track = null;
		if (markIt.hasNext()) {
			mark1Track = getTrackedRace().getOrCreateTrack(markIt.next());
			if (markIt.hasNext())
				mark2Track = getTrackedRace().getOrCreateTrack(markIt.next());
		}
		
		// calculate bearings from boat to 1st and 2nd mark and from 1st mark to 2nd mark and back
		Bearing mark1ToBoatBearing = null;
		Bearing mark2ToBoatBearing = null;
		Bearing mark2ToMark1Bearing = null;
		Bearing mark1ToMark2Bearing = null;
		try {
			mark1Track.lockForRead();
			try {
				mark2Track.lockForRead();
				mark1ToBoatBearing = mark1Track.getFirstRawFixAtOrAfter(fix.getTimePoint()).getPosition().getBearingGreatCircle(fix.getPosition());
				mark2ToBoatBearing = mark2Track.getFirstRawFixAtOrAfter(fix.getTimePoint()).getPosition().getBearingGreatCircle(fix.getPosition());
				mark2ToMark1Bearing = mark2Track.getFirstRawFixAtOrAfter(fix.getTimePoint()).getPosition().getBearingGreatCircle(mark1Track.getFirstRawFixAtOrAfter(fix.getTimePoint()).getPosition());
				mark1ToMark2Bearing = mark2ToMark1Bearing.reverse();
			} finally {
				mark2Track.unlockAfterRead();				
			}
		} finally {
			mark1Track.unlockAfterRead();
		}
		
		NauticSide passingSideOfMark1OfGate = getPassingSideOfGate(waypoint, fix.getTimePoint());
		double mark1PassingBearingDelta = mark1ToMark2Bearing.getDifferenceTo(mark1ToBoatBearing).getDegrees();
		double mark2PassingBearingDelta = mark2ToMark1Bearing.getDifferenceTo(mark2ToBoatBearing).getDegrees();
		
		if (	passingSideOfMark1OfGate.equals(NauticSide.STARBOARD) &&
				mark1PassingBearingDelta < 0 &&
				mark1PassingBearingDelta > -90 &&
				mark2PassingBearingDelta > 0 &&
				mark2PassingBearingDelta < 90) {
			// gate passed if mark1 had to be passed on stb
			return getDomainFactory().createMarkPassing(fix.getTimePoint(), waypoint, competitor);
			
		} else if (	passingSideOfMark1OfGate.equals(NauticSide.PORT) &&
				mark1PassingBearingDelta > 0 &&
				mark1PassingBearingDelta < 90 &&
				mark2PassingBearingDelta < 0 &&
				mark2PassingBearingDelta > -90) {
			// gate passed if mark1 had to be passed on port
			return getDomainFactory().createMarkPassing(fix.getTimePoint(), waypoint, competitor);
			
		}
		return null;
	}

	private NauticSide getPassingSideOfGate(Waypoint waypoint, TimePoint timePoint) {
		// calculate the passing side of the first mark in the waypoints marks
		Position waypointPos = getTrackedRace().getApproximatePosition(waypoint, timePoint);
		Position mark1Pos = null;
		
		DynamicGPSFixTrack<Mark, GPSFix> mark1Track = getTrackedRace().getOrCreateTrack(waypoint.getMarks().iterator().next());
		try {
			mark1Track.lockForRead();
			mark1Pos = mark1Track.getFirstRawFixAtOrAfter(timePoint).getPosition();
		} finally {
			mark1Track.unlockAfterRead();
		}
		
		if (getRace().getCourse().getFirstWaypoint().equals(waypoint)) {
			// first waypoint of the course, racing direction is towards next waypoint
			Position nextWpPos = getTrackedRace().getApproximatePosition(getNextWaypoint(waypoint), timePoint);
			Bearing mark1ToNextWp = mark1Pos.getBearingGreatCircle(nextWpPos);
			Bearing wpToNextWp = waypointPos.getBearingGreatCircle(nextWpPos);
			if (mark1ToNextWp.getDifferenceTo(wpToNextWp).getDegrees() > 0) {
				return NauticSide.PORT;
			} else {
				return NauticSide.STARBOARD;
			}
		} else {
			// racing direction is from previous waypoint
			Position prevWpPos = getTrackedRace().getApproximatePosition(getPreviousWaypoint(waypoint), timePoint);
			Bearing prevWpToMark1 = prevWpPos.getBearingGreatCircle(mark1Pos);
			Bearing prevWpToWp = prevWpPos.getBearingGreatCircle(waypointPos);
			if (prevWpToMark1.getDifferenceTo(prevWpToWp).getDegrees() < 0) {
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
