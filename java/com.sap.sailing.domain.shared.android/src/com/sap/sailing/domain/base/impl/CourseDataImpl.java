package com.sap.sailing.domain.base.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sse.common.impl.NamedImpl;

public class CourseDataImpl extends NamedImpl implements CourseBase {
    
    private static final long serialVersionUID = 2749443048689453078L;

    private final List<Waypoint> waypoints;

    private final UUID originatingCourseTemplateId; 

    private final Map<Mark, String> associatedRoles = new HashMap<>();

    public CourseDataImpl(String name) {
        this(name, null);
    }

    public CourseDataImpl(String name, UUID originatingCourseTemplateId) {
        super(name);
        this.waypoints = new ArrayList<Waypoint>();
        this.originatingCourseTemplateId = originatingCourseTemplateId;
    }

    @Override
    public List<Leg> getLegs() {
        return null;
    }

    @Override
    public Iterable<Waypoint> getWaypoints() {
        return this.waypoints;
    }

    @Override
    public Waypoint getWaypointForControlPoint(ControlPoint controlPoint, int start) {
        Waypoint result = null;
        List<Waypoint> subList = this.waypoints.subList(start, this.waypoints.size() - 1);
        for (Waypoint waypoint : subList) {
           if (waypoint.getControlPoint().equals(controlPoint)) {
               result = waypoint;
               break;
           }
        }
        return result;
    }

    @Override
    public int getIndexOfWaypoint(Waypoint waypoint) {
        return this.waypoints.indexOf(waypoint);
    }
    
    @Override
    public Waypoint getFirstWaypoint() {
        return this.waypoints.get(0);
    }

    @Override
    public Waypoint getLastWaypoint() {
        return this.waypoints.get(this.waypoints.size() - 1);
    }

    @Override
    public void addWaypoint(int zeroBasedPosition, Waypoint waypointToAdd) {
        this.waypoints.add(zeroBasedPosition, waypointToAdd);
    }

    @Override
    public void removeWaypoint(int zeroBasedPosition) {
        this.waypoints.remove(zeroBasedPosition);
    }

    @Override
    public Leg getFirstLeg() {
        return null;
    }

    @Override
    public UUID getOriginatingCourseTemplateIdOrNull() {
        return originatingCourseTemplateId;
    }

    public void addRoleMapping(Mark mark, String role) {
        associatedRoles.put(mark, role);
    }

    public Map<Mark, String> getAssociatedRoles() {
        return associatedRoles;
    }

}
