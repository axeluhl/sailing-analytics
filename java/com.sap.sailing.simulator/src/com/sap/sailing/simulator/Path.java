package com.sap.sailing.simulator;

import java.util.List;

import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.TimePoint;

public interface Path {
	
	List<TimedPositionWithSpeed> getPathPoints();
	
	void setPathPoints(List<TimedPositionWithSpeed> pointsList);
	
	TimedPositionWithSpeed getPositionAtTime(TimePoint t);
	
	List<TimedPositionWithSpeed> getEvenTimedPath(long timeStep);
	
	List<TimedPositionWithSpeed> getEvenTimedPoints(long milliseconds);
	
	List<TimedPositionWithSpeed> getEvenDistancedPoints(Distance dist);
	
	void setWindField(WindField wf);

}
