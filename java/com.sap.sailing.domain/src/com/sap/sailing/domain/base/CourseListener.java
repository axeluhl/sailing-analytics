package com.sap.sailing.domain.base;

public interface CourseListener {
    void waypointAdded(int zeroBasedIndex, Waypoint waypointThatGotAdded);

    void waypointRemoved(int zeroBasedIndex, Waypoint waypointThatGotRemoved);
}
