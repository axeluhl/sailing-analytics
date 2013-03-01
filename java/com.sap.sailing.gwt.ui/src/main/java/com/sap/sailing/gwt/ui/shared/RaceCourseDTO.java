package com.sap.sailing.gwt.ui.shared;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

public class RaceCourseDTO implements IsSerializable {
    public List<WaypointDTO> waypoints;

    RaceCourseDTO() {}

    public RaceCourseDTO(List<WaypointDTO> waypoints) {
        this.waypoints = waypoints;
    }

    public List<ControlPointDTO> getControlPoints() {
        List<ControlPointDTO> controlPoints = new ArrayList<ControlPointDTO>();
        for(WaypointDTO waypoint: waypoints) {
            controlPoints.add(waypoint.controlPoint);
        }
        return controlPoints;
    }

    public Collection<MarkDTO> getMarks() {
        Map<String, MarkDTO> marks = new HashMap<String, MarkDTO>();
        for(WaypointDTO waypoint: waypoints) {
            for(MarkDTO mark: waypoint.marks) {
                if(!marks.containsKey(mark.name)) {
                    marks.put(mark.name, mark);
                }
            }
        }
        return marks.values();
    }
}
