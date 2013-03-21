package com.sap.sailing.domain.base;

public interface CourseListener {
    void waypointAdded(int zeroBasedIndex, Waypoint waypointThatGotAdded);

    /**
     * @param waypointThatGotRemoved
     *            Note that the waypoint already has been removed. You have to expect exceptions if trying to perform
     *            methods on adjacent legs that would refer to the waypoint that already got removed, in particular
     *            calling {@link Leg#getFrom()} on the leg that starts at the waypoint removed if this was the last
     *            waypoint in the course.
     */
    void waypointRemoved(int zeroBasedIndex, Waypoint waypointThatGotRemoved);
}
