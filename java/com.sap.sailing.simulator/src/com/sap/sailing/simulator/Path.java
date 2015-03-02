package com.sap.sailing.simulator;

import java.util.List;

import com.sap.sailing.simulator.windfield.WindField;

public interface Path {

    List<TimedPositionWithSpeed> getPathPoints();

    void setPathPoints(List<TimedPositionWithSpeed> pointsList);
    
    public long getMaxTurnTime();
    
    public void setMaxTurnTime(long maxTurnTime);

    void setWindField(WindField wf);

    List<TimedPositionWithSpeed> getTurns();

    Path getEvenTimedPath(long timeStep);
}
