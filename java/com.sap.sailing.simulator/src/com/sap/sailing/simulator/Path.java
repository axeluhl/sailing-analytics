package com.sap.sailing.simulator;

import java.util.List;

import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.TimePoint;

public interface Path {
	
	List<TimedPositionWithSpeed> getPathPoints();
	
	void setPathPoints(List<TimedPositionWithSpeed> pointsList);
	
	TimedPosition getPositionAtTime(TimePoint t);
	
	List<TimedPosition> getEvenTimedPoints(long milliseconds);
	
	List<TimedPosition> getEvenDistancedPoints(Distance dist);

}
