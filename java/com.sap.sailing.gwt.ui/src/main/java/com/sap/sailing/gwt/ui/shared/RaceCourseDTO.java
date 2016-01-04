package com.sap.sailing.gwt.ui.shared;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

public class RaceCourseDTO implements IsSerializable {
    public List<WaypointDTO> waypoints;
    
    /**
     * List of all marks in race, that can include such that are not actually part of
     * the race course definition.
     */
    private List<MarkDTO> allMarks;

    RaceCourseDTO() {
    }

    public RaceCourseDTO(List<WaypointDTO> waypoints) {
        this(waypoints, getMarksFromWaypoints(waypoints));
    }

    private static List<MarkDTO> getMarksFromWaypoints(List<WaypointDTO> waypoints) {
        final LinkedHashSet<MarkDTO> marks = new LinkedHashSet<>();
        for (final WaypointDTO waypoint : waypoints) {
            for (final MarkDTO m : waypoint.controlPoint.getMarks()) {
                marks.add(m);
            }
        }
        final List<MarkDTO> result = new ArrayList<>();
        for (final MarkDTO m : marks) {
            result.add(m);
        }
        return result;
    }

    public RaceCourseDTO(List<WaypointDTO> waypoints, List<MarkDTO> allMarks) {
        this.waypoints = waypoints;
        this.allMarks = allMarks;
    }

    public List<ControlPointDTO> getControlPoints() {
        List<ControlPointDTO> controlPoints = new ArrayList<ControlPointDTO>();
        for (WaypointDTO waypoint : waypoints) {
            controlPoints.add(waypoint.controlPoint);
        }
        return controlPoints;
    }

    public Collection<MarkDTO> getMarks() {
        Collection<MarkDTO> result;
        if (allMarks != null) {
            result = allMarks;
        } else {
            Map<String, MarkDTO> marks = new HashMap<String, MarkDTO>();
            for (WaypointDTO waypoint : waypoints) {
                for (MarkDTO mark : waypoint.controlPoint.getMarks()) {
                    if (!marks.containsKey(mark.getName())) {
                        marks.put(mark.getName(), mark);
                    }
                }
            }
            result = marks.values();
        }
        return result;
    }
}
