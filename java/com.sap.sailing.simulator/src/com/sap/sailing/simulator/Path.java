package com.sap.sailing.simulator;

import java.util.List;

import com.sap.sailing.simulator.windfield.WindField;

public interface Path {

    List<TimedPositionWithSpeed> getPathPoints();

    void setPathPoints(List<TimedPositionWithSpeed> pointsList);

    List<TimedPositionWithSpeed> getEvenTimedPath(long timeStep);

    // TimedPositionWithSpeed getPositionAtTime(TimePoint t);

    // List<TimedPositionWithSpeed> getEvenTimedPoints(long milliseconds);

    // List<TimedPositionWithSpeed> getEvenDistancedPoints(Distance dist);

    void setWindField(WindField wf);

    // TimedPositionWithSpeed getClosestTurn(double lngDeg, double latDeg);

    List<TimedPositionWithSpeed> getTurns();

    Path getEvenTimedPath2(long timeStep);
}
