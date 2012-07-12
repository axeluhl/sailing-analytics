package com.sap.sailing.gwt.ui.shared;

import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class RaceCourseDTO implements IsSerializable {
    public WaypointDTO startWaypoint;
    public WaypointDTO finishWaypoint;
    
    public List<WaypointDTO> waypoints;
    
    public Date requestTime;

    public WaypointDTO getFirstWaypoint() {
        if(waypoints.size() == 0) {
            return null;
        }
        return waypoints.get(0);
    }

    public WaypointDTO getLastWaypoint() {
        if(waypoints.size() == 0) {
            return null;
        }
        return waypoints.get(waypoints.size()-1);
    }
}
