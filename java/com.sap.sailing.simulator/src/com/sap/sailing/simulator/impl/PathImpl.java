package com.sap.sailing.simulator.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.simulator.Path;
import com.sap.sailing.simulator.TimedPositionWithSpeed;
import com.sap.sailing.simulator.WindField;

public class PathImpl implements Path {

	List<TimedPositionWithSpeed>  pathPoints;
	WindField windField;
	
	public PathImpl(List<TimedPositionWithSpeed> pointsList, WindField wf) {
		
		pathPoints = pointsList;
		windField = wf;
		
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
	public TimedPositionWithSpeed getPositionAtTime(TimePoint t) {
		
		if (t.compareTo(pathPoints.get(0).getTimePoint()) == 0) return pathPoints.get(0); 
                if (t.compareTo(pathPoints.get(pathPoints.size()-1).getTimePoint()) >= 0) return pathPoints.get(pathPoints.size()-1);
		
		TimedPositionWithSpeed p1 = null;
		TimedPositionWithSpeed p2 = null;
		for (TimedPositionWithSpeed p : pathPoints) {
			if (p.getTimePoint().compareTo(t) >= 0) {
				p2 = p;
				p1 = pathPoints.get(pathPoints.indexOf(p)-1);
				break;
			}
		}
	
		double t1 = 1000.0 * p1.getTimePoint().asMillis();
		double t2 = 1000.0 * p2.getTimePoint().asMillis();
		double t0 = 1000.0 * t.asMillis();
		
		Distance dist = p1.getPosition().getDistance(p2.getPosition());
		Position p0 = p1.getPosition().translateGreatCircle(p1.getPosition().getBearingGreatCircle(p2.getPosition()), dist.scale((t0-t1)/(t2-t1)));
		SpeedWithBearing windAtPoint = windField.getWind(new TimedPositionImpl(t, p0));
		
		return new TimedPositionWithSpeedImpl(t, p0, windAtPoint);
	}

	@Override
	public List<TimedPositionWithSpeed> getEvenTimedPoints(long milliseconds) {
		
	        if (milliseconds == 0) return null;
	    
		List<TimedPositionWithSpeed> lst = new ArrayList<TimedPositionWithSpeed>();
		TimePoint t = pathPoints.get(0).getTimePoint();
		TimePoint lastPoint = pathPoints.get(pathPoints.size()-1).getTimePoint();

		while((t.compareTo(lastPoint) <= 0)&&(lst.size() < 200)) { // paths with more than 200 points lead to performance issues
			lst.add(getPositionAtTime(t));
			t = new MillisecondsTimePoint(t.asMillis() + milliseconds);
		}
		if (t.compareTo(lastPoint)> 0) { // without this, the path may not reach end
                    lst.add(getPositionAtTime(t));
		}
		//if (!lst.contains(pathPoints.get(pathPoints.size()-1)))
		//	lst.add(pathPoints.get(pathPoints.size()-1));
		
		return lst;
	}

	//not implemented yet!
	@Override
	public List<TimedPositionWithSpeed> getEvenDistancedPoints(Distance dist) {
		return null;
	}

	@Override
	public void setWindField(WindField wf) {

		windField = wf;
		
	}
	
}
