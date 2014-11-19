package com.sap.sailing.domain.test.markpassing;

import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Before;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.markpassingcalculation.splining.HermiteCurve;
import com.sap.sailing.domain.markpassingcalculation.splining.StraightLine;
import com.sap.sailing.domain.markpassingcalculation.splining.Vector2D;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sse.common.TimePoint;

/**
 * This class tests a mark passing detection algorithm that interpolates the boats' positions between
 * their GPS fixes using Hermite Splines.
 * @author Martin Hanysz
 *
 */
public class MartinMarkPassingSplineBasedTest extends MartinAbstractMarkPassingTest {
	
	// a factor to scale the tangents calculated from boat speed and bearing
	private static double TANGENT_SCALING_FACTOR = 100;
	private Map<Competitor, Entry<MarkPassing, Position>> lastCompetitorPassings;

	public MartinMarkPassingSplineBasedTest() throws MalformedURLException, URISyntaxException {
		super();
		lastCompetitorPassings = new HashMap<Competitor, Entry<MarkPassing, Position>>();
	}
	
	@Before
	public void setTangentScaling() {
		TANGENT_SCALING_FACTOR = 100.0;
	}
	

	public void testTangentScaling() {
		Map<Double, Integer> results = new HashMap<Double, Integer>();
		double s = 100.0;
		double div = 10.0;
		while (s > 0.000000001) {
			MartinMarkPassingSplineBasedTest.TANGENT_SCALING_FACTOR = s;
			List<MarkPassing> markPassings = computeAllMarkPassings();
			// compare computed mark passings to given ones
			int overallMisses = compareCalculatedAndGivenPassings(markPassings, false);
			results.put(s, overallMisses);
			s /= div;
		}
		boolean success = false;
		for (Entry<Double, Integer> e : results.entrySet()) {
			System.out.println("Scaling: " + e.getKey() + ", Misses: " + e.getValue());
			if (e.getValue() == 0) {
				success = true;
			}
		}
		assertTrue("No setting for the scaling factor resulted in 0 missed waypoints.", success);
	}
	

	/* (non-Javadoc)
	 * @see com.sap.sailing.domain.test.AbstractMarkPassingTest#computeMarkPassings()
	 */
	@Override
	MarkPassing computeMarkPassings(Competitor competitor, GPSFixMoving fix) {
		// interpolate the course of the competitor from the previous fix to the given fix
		DynamicGPSFixTrack<Competitor, GPSFixMoving> competitorTrack = getTrackedRace().getTrack(competitor);
		GPSFixMoving prevFix = null;
		try {
			competitorTrack.lockForRead();
			prevFix = competitorTrack.getLastRawFixBefore(fix.getTimePoint());
		} finally {
			competitorTrack.unlockAfterRead();
		}
		if (prevFix == null) {
			return null;
		}
		Vector2D point1 = new Vector2D(prevFix.getPosition());
		Vector2D point2 = new Vector2D(fix.getPosition());
		Vector2D tangent1 = new Vector2D(prevFix.getSpeed().getBearing(), prevFix.getSpeed().getKnots() * TANGENT_SCALING_FACTOR /* abh�ngig von Bootsklasse, zeitl. Abstand zw. Fixes */);
		Vector2D tangent2 = new Vector2D(fix.getSpeed().getBearing(), fix.getSpeed().getKnots() * TANGENT_SCALING_FACTOR);
		HermiteCurve interpolatedCourse = new HermiteCurve(point1, point2, tangent1, tangent2);

		// find out which waypoints to check (last waypoint passed and the next)
		List<Waypoint> waypointsToCheck = new ArrayList<Waypoint>();
		Course raceCourse = getRace().getCourse();
		try {
			raceCourse.lockForRead();
			if (!lastCompetitorPassings.containsKey(competitor)) {
				// no previous passings, competitor needs to pass 1st waypoint
				waypointsToCheck.add(raceCourse.getFirstWaypoint());
			} else {
				waypointsToCheck.add(lastCompetitorPassings.get(competitor).getKey().getWaypoint());
				Waypoint next = getNextWaypoint(lastCompetitorPassings.get(competitor).getKey().getWaypoint());
				if (next != null) {
					waypointsToCheck.add(next);
				}
			}
		} finally {
			raceCourse.unlockAfterRead();
		}
		
		// check if the interpolated course passes one of the waypoints to check
		Map<MarkPassing, Position> possiblePassings = new HashMap<MarkPassing, Position>();
		for (Waypoint wp : waypointsToCheck) {
			StraightLine passingLine = null;
			List<Position> markPositions = new ArrayList<Position>();
			for (Mark m : wp.getMarks()) {
				DynamicGPSFixTrack<Mark, GPSFix> markTrack = getTrackedRace().getOrCreateTrack(m);
				try {
					markTrack.lockForRead();
					markPositions.add(markTrack.getLastFixAtOrBefore(fix.getTimePoint()).getPosition());
				} finally {
					markTrack.unlockAfterRead();
				}
			}
			Vector2D mark1Pos = new Vector2D(markPositions.get(0));
			Map<Vector2D, Double> intersections = null;
			//Mark passedMark = wp.getMarks().iterator().next();
			if (isGate(wp)) {
				if (wp.equals(raceCourse.getFirstWaypoint()) || wp.equals(raceCourse.getLastWaypoint())) {
					// passing start or finish line
					Vector2D mark2Pos = new Vector2D(markPositions.get(1));
					passingLine = new StraightLine(mark1Pos, mark2Pos.subtract(mark1Pos));	
					intersections = interpolatedCourse.intersectWith(passingLine);
				} else {
					// passing either mark of the gate
					for (Mark mark : wp.getMarks()) {
						Bearing passingBearing = getPassingBearing(wp, mark, fix.getTimePoint());
						Vector2D passingVector = new Vector2D(passingBearing, 1.0);
						passingLine = new StraightLine(mark1Pos, passingVector);
						intersections = interpolatedCourse.intersectWith(passingLine);
						if (intersections.size() > 0) {
							// already found a passing of a mark
							//passedMark = mark;
							break;
						}
					}
				}
			} else {
				// passing single buoy
				Bearing passingBearing = getPassingBearing(wp, wp.getMarks().iterator().next(), fix.getTimePoint());
				Vector2D passingVector = new Vector2D(passingBearing, 1.0);
				passingLine = new StraightLine(mark1Pos, passingVector);
				intersections = interpolatedCourse.intersectWith(passingLine);
			}
			for (Entry<Vector2D, Double> entry : intersections.entrySet()) {
				TimePoint time = fix.getTimePoint();
				TimePoint prevTime = prevFix.getTimePoint();
				// t_pass = t_fix - (t_prev - t_fix) * interpolationParameterOfIntersection
				// If the intersection is at 75% of the interpolated course, t_pass is at 75% between t_prev and t_fix.
				// This cast truncates the milliseconds, which is acceptable at the moment.
				TimePoint timePoint = time.minus((long) ((prevTime.asMillis() - time.asMillis()) * entry.getValue()));
				possiblePassings.put(DomainFactory.INSTANCE.createMarkPassing(timePoint, wp, competitor), new DegreePosition(entry.getKey().y(), entry.getKey().x()));
			}
		}
		Entry<MarkPassing, Position> result = null;
		if (possiblePassings.size() > 0) {
			Entry<MarkPassing, Position> lastPassing = lastCompetitorPassings.get(competitor);
			Entry<MarkPassing, Position> possiblePassing = possiblePassings.entrySet().iterator().next();
			if (lastPassing != null &&
				lastPassing.getKey().getWaypoint().equals(possiblePassing.getKey().getWaypoint())) {
				// previous passing detected & passed same waypoint again
				possiblePassings.put(lastPassing.getKey(), lastPassing.getValue());
			} 
			result = findBestMarkPassing(possiblePassings);
		}
		if (result != null) {			
			lastCompetitorPassings.put(competitor, result);
			return result.getKey();
		} else {
			return null;
		}
	}
}
