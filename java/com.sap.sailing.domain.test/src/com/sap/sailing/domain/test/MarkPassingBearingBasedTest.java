package com.sap.sailing.domain.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;

/**
 * This class tests a mark passing detection algorithm that is based purely on bearing analyses
 * from the {@link Course}'s {@link Mark}s to the GPS fixes of the {@link Competitor}'s boats.
 * It uses a "hot zone" around the {@link Mark}s to avoid unnecessary testing of {@link Mark}s
 * too far away from the boat.
 * 
 * @author Martin Hanysz
 *
 */
public class MarkPassingBearingBasedTest extends AbstractMarkPassingTest {

	/**
	 * Controls the size of the hot zone around buoys. It is multiplied with the boat length.
	 */
	private static final double HOT_ZONE_TO_BOAT_LENGHT_RATIO = 10.0;
	
	/**
	 * Controls how long a new mark passing will overwrite the previous one for the same mark.
	 * If a new mark passing is detected for the same {@link Waypoint} within MARK_PASSING_CORRECTION_BOUNDARY milliseconds, the previous one is overwritten.
	 */
	static final long MARK_PASSING_CORRECTION_BOUNDARY = 100000;
	
	public MarkPassingBearingBasedTest() throws MalformedURLException, URISyntaxException {
		super();
	}

	@Override
	Map<Competitor, Map<Waypoint, List<List<MarkPassing>>>> computeMarkPassings() {
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
		return markPassings;
	}

	/**
	 * Check if the given {@link Waypoint} was passed with the given {@link GPSFixMoving} of the given {@link Competitor}. 
	 * @param waypoint - the {@link Waypoint} to check
	 * @param fix - the {@link GPSFixMoving} to check
	 * @param competitor - the {@link Competitor} the given fix belongs to
	 * @return a {@link MarkPassing} iff the fix represents a passing of the given {@link Waypoint}, null if not
	 */
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
			NauticalSide passingSide = getPassingSideOfMark(waypoint, mark, fix.getTimePoint());
			if (passingSide != null && passingSide.equals(NauticalSide.STARBOARD)) {	
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

	/**
	 * Check if the line represented by the given {@link Waypoint} was passed with the given {@link GPSFixMoving} of the given {@link Competitor}. 
	 * @param waypoint - the {@link Waypoint} to check
	 * @param fix - the {@link GPSFixMoving} to check
	 * @param competitor - the {@link Competitor} the given fix belongs to
	 * @return a {@link MarkPassing} iff the fix represents a passing of the line of the given {@link Waypoint}, null if not
	 */
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
		
		NauticalSide passingSideOfMark1OfGate = getPassingSideForMark1OfGate(waypoint, fix.getTimePoint());
		double mark1PassingBearingDelta = boatToMark1Bearing.getDifferenceTo(mark2ToMark1Bearing).getDegrees();
		double mark2PassingBearingDelta = boatToMark2Bearing.getDifferenceTo(mark1ToMark2Bearing).getDegrees();
		
		if (	passingSideOfMark1OfGate.equals(NauticalSide.STARBOARD) &&
				mark1PassingBearingDelta < 0 &&				
				mark2PassingBearingDelta > 0) {
			// gate passed if mark1 had to be passed on stb
			return getDomainFactory().createMarkPassing(fix.getTimePoint(), waypoint, competitor);
		} else if (	passingSideOfMark1OfGate.equals(NauticalSide.PORT) &&
				mark1PassingBearingDelta > 0 &&				
				mark2PassingBearingDelta < 0) {
			// gate passed if mark1 had to be passed on port
			return getDomainFactory().createMarkPassing(fix.getTimePoint(), waypoint, competitor);
		}
		return null;
	}
}
