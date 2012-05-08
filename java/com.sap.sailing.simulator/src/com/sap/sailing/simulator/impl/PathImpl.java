package com.sap.sailing.simulator.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.base.impl.KilometersPerHourSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.simulator.Path;
import com.sap.sailing.simulator.TimedPosition;
import com.sap.sailing.simulator.TimedPositionWithSpeed;

public class PathImpl implements Path {

	List<TimedPositionWithSpeed>  pathPoints;
	
	public PathImpl(List<TimedPositionWithSpeed> pointsList) {
		
		pathPoints = pointsList;
		
	}
	
	@Override
	public List<TimedPositionWithSpeed> getPathPoints() {
		return pathPoints;
	}

	@Override
	public void setPathPoints(List<TimedPositionWithSpeed> pointsList) {
		pathPoints = pointsList;
	}

	@Override
	public TimedPosition getPositionAtTime(TimePoint t) {
		
		if (t.compareTo(pathPoints.get(0).getTimePoint()) == 0) return pathPoints.get(0); 
		
		TimedPositionWithSpeed p1 = null;
		TimedPositionWithSpeed p2 = null;
		for (TimedPositionWithSpeed p : pathPoints) {
			if (p.getTimePoint().compareTo(t) >= 0) {
				p2 = p;
				p1 = pathPoints.get(pathPoints.indexOf(p)-1);
				break;
			}
		}
		/*double v1 = p1.getSpeed().getMetersPerSecond();
		double v2 = p2.getSpeed().getMetersPerSecond();
		double t1 = 1000.0 * p1.getTimePoint().asMillis();
		double t2 = 1000.0 * p2.getTimePoint().asMillis();
		double t0 = 1000.0 * t.asMillis();
		double acc = (v2-v1)/(t2-t1);
		Distance dist = new MeterDistance(v1*t0 + acc*t0*t0/2);
		Position p0 = p1.getPosition().
				translateGreatCircle(p1.getPosition().getBearingGreatCircle(p2.getPosition()), dist); */
		
		double t1 = 1000.0 * p1.getTimePoint().asMillis();
		double t2 = 1000.0 * p2.getTimePoint().asMillis();
		double t0 = 1000.0 * t.asMillis();
		
		Distance dist = p1.getPosition().getDistance(p2.getPosition());
		Position p0 = p1.getPosition().translateGreatCircle(p1.getPosition().getBearingGreatCircle(p2.getPosition()), dist.scale((t0-t1)/(t2-t1)));
		
		return new TimedPositionImpl(t, p0);
	}

	@Override
	public List<TimedPosition> getEvenTimedPoints(long milliseconds) {
		
		List<TimedPosition> lst = new ArrayList<TimedPosition>();
		TimePoint t = pathPoints.get(0).getTimePoint();
		TimePoint lastPoint = pathPoints.get(pathPoints.size()-1).getTimePoint();

		while(t.compareTo(lastPoint) <= 0) {
			lst.add(getPositionAtTime(t));
			t = new MillisecondsTimePoint(t.asMillis() + milliseconds);
		}
		return lst;
	}

	@Override
	public List<TimedPosition> getEvenDistancedPoints(Distance dist) {
		return null;
	}
	
	/*public static void main(String[] args) {
		
		TimedPositionWithSpeed p1 = new TimedPositionWithSpeedImpl(new MillisecondsTimePoint(0), new DegreePosition(25.045792, -91.472168), new KilometersPerHourSpeedWithBearingImpl(30, null));
		TimedPositionWithSpeed p2 = new TimedPositionWithSpeedImpl(new MillisecondsTimePoint(3000000), new DegreePosition(26.076521,-89.681396), new KilometersPerHourSpeedWithBearingImpl(30, null));
		TimedPositionWithSpeed p3 = new TimedPositionWithSpeedImpl(new MillisecondsTimePoint(6000000), new DegreePosition(26.495157,-87.698364), new KilometersPerHourSpeedWithBearingImpl(30, null));
		
		List<TimedPositionWithSpeed> lst = new ArrayList<TimedPositionWithSpeed>();
		lst.add(p1);
		lst.add(p2);
		lst.add(p3);
		
		Path pth = new PathImpl(lst);
		System.out.println(pth.getPositionAtTime(new MillisecondsTimePoint(0)).getPosition());
		for (TimedPositionWithSpeed p : pth.getPathPoints()) {
			System.out.println(p.getPosition());
			System.out.println(p.getTimePoint());
		}
		System.out.println(pth.getPositionAtTime(new MillisecondsTimePoint(0)).getPosition());
		for(TimedPosition p : pth.getEvenTimedPoints(500000)) {
			System.out.println(p.getTimePoint());
			System.out.println(p.getPosition());
		}
	}*/

}
