package com.sap.sailing.simulator;

import java.util.List;

import com.sap.sailing.simulator.windfield.WindField;
import com.sap.sse.common.TimePoint;

public interface Path {

    List<TimedPositionWithSpeed> getPathPoints();

    void setPathPoints(List<TimedPositionWithSpeed> pointsList);
    
    public long getMaxTurnTime();
    
    public void setMaxTurnTime(long maxTurnTime);

    void setWindField(WindField wf);

    List<TimedPositionWithSpeed> getTurns();
    
    TimePoint getFinalTime();

    Path getEvenTimedPath(long timeStep);
    
    int getTurnCount();
    
    boolean getAlgorithmTimedOut();

    boolean getMixedLeg();
}
