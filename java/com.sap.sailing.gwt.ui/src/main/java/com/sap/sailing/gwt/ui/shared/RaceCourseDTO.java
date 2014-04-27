package com.sap.sailing.gwt.ui.shared;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

public class RaceCourseDTO implements IsSerializable {
    public List<WaypointDTO> waypoints;
    public List<MarkDTO> marks;

    RaceCourseDTO() {
    }

    public RaceCourseDTO(List<WaypointDTO> waypoints) {
        this(waypoints, null);
    }

    public RaceCourseDTO(List<WaypointDTO> waypoints, List<MarkDTO> marks) {
        this.waypoints = waypoints;
        this.marks = marks;
    }

    public List<ControlPointDTO> getControlPoints() {
        List<ControlPointDTO> controlPoints = new ArrayList<ControlPointDTO>();
        for (WaypointDTO waypoint : waypoints) {
            controlPoints.add(waypoint.controlPoint);
        }
        return controlPoints;
    }

    public Collection<MarkDTO> getMarks() {
        final Collection<MarkDTO> result;
        if (marks != null) {
            result = marks;
        } else {
            Map<String, MarkDTO> marks = new HashMap<String, MarkDTO>();
            for (WaypointDTO waypoint : waypoints) {
                for (MarkDTO mark : waypoint.marks) {
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
