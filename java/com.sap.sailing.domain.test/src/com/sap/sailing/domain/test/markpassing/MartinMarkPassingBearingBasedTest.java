package com.sap.sailing.domain.test.markpassing;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
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
public class MartinMarkPassingBearingBasedTest extends MartinAbstractMarkPassingTest {

	/**
	 * Controls the size of the hot zone around buoys. It is multiplied with the boat length.
	 */
	private static final double HOT_ZONE_TO_BOAT_LENGHT_RATIO = 10.0;

	private Map<Competitor, Entry<MarkPassing, Position>> lastCompetitorPassings;
	
	public MartinMarkPassingBearingBasedTest() throws MalformedURLException, URISyntaxException {
		super();
		lastCompetitorPassings = new HashMap<Competitor, Entry<MarkPassing, Position>>();
	}

	@Override
	MarkPassing computeMarkPassings(Competitor competitor, GPSFixMoving fix) {
		Map<MarkPassing, Position> possibleMarkPassings = new HashMap<MarkPassing, Position>();
		
		// find out which waypoints to check (last waypoint passed and the next)
		List<Waypoint> waypointsToCheck = new ArrayList<Waypoint>();
		Course course = getRace().getCourse();
		try {
			course.lockForRead();
			if (!lastCompetitorPassings.containsKey(competitor)) {
				// no previous passings, competitor needs to pass 1st waypoint
				waypointsToCheck.add(course.getFirstWaypoint());
			} else {
				// previous passing, competitor either passes same waypoint again or the next waypoint
				waypointsToCheck.add(lastCompetitorPassings.get(competitor).getKey().getWaypoint());
				Waypoint next = getNextWaypoint(lastCompetitorPassings.get(competitor).getKey().getWaypoint());
				if (next != null) {
					waypointsToCheck.add(next);
				}
			}
		} finally {
			course.unlockAfterRead();
		}
		
		for (Waypoint wp : waypointsToCheck) {
			MarkPassing passing = null;
			if (isGate(wp)) {
				if (wp.equals(course.getFirstWaypoint()) || 
					wp.equals(course.getLastWaypoint())) {
					// start or finish line
					passing = getLinePassing(wp, fix, competitor);
				} else {
					// waypoint is a gate, either pass mark 1 or mark 2 of the gate
					Iterator<Mark> markIterator = wp.getMarks().iterator();
					while (passing == null && markIterator.hasNext()) {
						passing = getBuoyPassing(wp, markIterator.next(), fix, competitor);						
					}
				}
			} else {
				// waypoint is single buoy
				passing = getBuoyPassing(wp, wp.getMarks().iterator().next(), fix, competitor);
			}
			
			if (passing != null) {
				// detected a passing
				possibleMarkPassings.put(passing, fix.getPosition());
			}
		}
		Entry<MarkPassing, Position> result = null;
		if (possibleMarkPassings.size() > 0) {
			Entry<MarkPassing, Position> lastPassing = lastCompetitorPassings.get(competitor);
			Entry<MarkPassing, Position> possiblePassing = possibleMarkPassings.entrySet().iterator().next();
			if (lastPassing != null &&
				lastPassing.getKey().getWaypoint().equals(possiblePassing.getKey().getWaypoint())) {
				// previous passing detected & passed same waypoint again
				possibleMarkPassings.put(lastPassing.getKey(), lastPassing.getValue());
			} 
			result = findBestMarkPassing(possibleMarkPassings);
		}
		if (result != null) {			
			lastCompetitorPassings.put(competitor, result);
			return result.getKey();
		} else {
			return null;
		}
	}

	/**
	 * Check if the given {@link Waypoint} was passed with the given {@link GPSFixMoving} of the given {@link Competitor}. 
	 * @param waypoint - the {@link Waypoint} to check
	 * @param mark - the mark of the given {@link Waypoint} to be checked
	 * @param fix - the {@link GPSFixMoving} to check
	 * @param competitor - the {@link Competitor} the given fix belongs to
	 * @return a {@link MarkPassing} if the fix represents a passing of the given {@link Waypoint}, null if not
	 */
	private MarkPassing getBuoyPassing(Waypoint waypoint, Mark mark, GPSFixMoving fix, Competitor competitor) {
		if (waypoint == null || getTrackedRace().getStartOfRace().compareTo(fix.getTimePoint()) > 0) {
			// no waypoint or race not started yet
			return null;
		}

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
			return null;
		}
		
		// calculate mark passing bearing for waypoint
		Bearing markToBoatBearing = markPos.getBearingGreatCircle(fix.getPosition());
		Bearing passingBearing = getPassingBearing(waypoint, mark, fix.getTimePoint());					
		PassingInstruction passingInstruction = getPassingInstructionOfMark(waypoint, mark, fix.getTimePoint());
		double passingBearingDelta = markToBoatBearing.getDifferenceTo(passingBearing).getDegrees();
		if (passingInstruction == null || 
				(passingInstruction.equals(PassingInstruction.Port) &&
				passingBearingDelta > 0)) {
			// markToBoatBearing is smaller than passing bearing -> passed on port
			return DomainFactory.INSTANCE.createMarkPassing(fix.getTimePoint(), waypoint, competitor);
		} else if (	passingInstruction.equals(PassingInstruction.Starboard) &&
					passingBearingDelta < 0) {
			// markToBoatBearing is greater than passing bearing -> passed on stb
			return DomainFactory.INSTANCE.createMarkPassing(fix.getTimePoint(), waypoint, competitor);
		}
		return null;
	}

	/**
	 * Check if the line represented by the given {@link Waypoint} was passed with the given {@link GPSFixMoving} of the given {@link Competitor}. 
	 * @param waypoint - the {@link Waypoint} to check
	 * @param fix - the {@link GPSFixMoving} to check
	 * @param competitor - the {@link Competitor} the given fix belongs to
	 * @return a {@link MarkPassing} if the fix represents a passing of the line of the given {@link Waypoint}, null if not
	 */
	private MarkPassing getLinePassing(Waypoint waypoint, GPSFixMoving fix, Competitor competitor) {
		if (waypoint == null || !isGate(waypoint)) {
			// no waypoint, waypoint not a gate, or race not started yet
			return null;
		}
		
		/* A boat passes the line, if the bearing from the boat to mark 1 of the gate equals 
		 * the bearing from mark 2 of the gate to mark 1 of the gate AND the bearing from the boat
		 * to mark 2 of the gate equals the bearing from mark 1 of the gate to mark 2 of the gate.
		 */
		// get tracks of both marks
		Iterator<Mark> markIt = waypoint.getMarks().iterator();
		DynamicGPSFixTrack<Mark, GPSFix> mark1Track = null;
		Mark mark1 = null;
		DynamicGPSFixTrack<Mark, GPSFix> mark2Track = null;
		Mark mark2 = null;
		if (markIt.hasNext()) {
			mark1 = markIt.next();
			mark1Track = getTrackedRace().getOrCreateTrack(mark1);
			if (markIt.hasNext())
				mark2 = markIt.next();
				mark2Track = getTrackedRace().getOrCreateTrack(mark2);
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
		
		PassingInstruction PassingInstructionOfMark1OfGate = getPassingInstructionForMark1OfGate(waypoint, fix.getTimePoint());
		double mark1PassingBearingDelta = boatToMark1Bearing.getDifferenceTo(mark2ToMark1Bearing).getDegrees();
		double mark2PassingBearingDelta = boatToMark2Bearing.getDifferenceTo(mark1ToMark2Bearing).getDegrees();
		//Mark closestMark = mark1;
		if (mark2 != null && fix.getPosition().getDistance(mark1Pos).compareTo(fix.getPosition().getDistance(mark2Pos)) < 0) {
			//closestMark = mark2;
			
		}
		
		if (	PassingInstructionOfMark1OfGate.equals(PassingInstruction.Starboard) &&
				mark1PassingBearingDelta < 0 &&				
				mark2PassingBearingDelta > 0) {
			// gate passed if mark1 had to be passed on stb
			return DomainFactory.INSTANCE.createMarkPassing(fix.getTimePoint(), waypoint,competitor);
		} else if (	PassingInstructionOfMark1OfGate.equals(PassingInstruction.Port) &&
				mark1PassingBearingDelta > 0 &&				
				mark2PassingBearingDelta < 0) {
			// gate passed if mark1 had to be passed on port
			return DomainFactory.INSTANCE.createMarkPassing(fix.getTimePoint(), waypoint, competitor);
		}
		return null;
	}
}
