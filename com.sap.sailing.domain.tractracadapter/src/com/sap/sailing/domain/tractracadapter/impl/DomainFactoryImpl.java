package com.sap.sailing.domain.tractracadapter.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.BuoyGateImpl;
import com.sap.sailing.domain.base.impl.BuoyImpl;
import com.sap.sailing.domain.base.impl.BuoyMarkImpl;
import com.sap.sailing.domain.base.impl.CourseImpl;
import com.sap.sailing.domain.base.impl.DegreePosition;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.tractrac.clientmodule.ControlPoint;
import com.tractrac.clientmodule.data.RouteData;

public class DomainFactoryImpl implements DomainFactory {

    @Override
    public Position createPosition(
            com.tractrac.clientmodule.data.Position position) {
        return new DegreePosition(position.getLatitude(), position.getLongitude());
    }

    @Override
    public TimePoint createTimePoint(long timestamp) {
        return new MillisecondsTimePoint(timestamp);
    }

    @Override
    public Waypoint createWaypoint(ControlPoint controlPoint) {
        if (controlPoint.getHasTwoPoints()) {
            // it's a gate
            return new BuoyGateImpl(new BuoyImpl(controlPoint.getName()+" (left)"),
                    new BuoyImpl(controlPoint.getName()+" (right)"), controlPoint.getName());
        } else {
            return new BuoyMarkImpl(controlPoint.getName());
        }
    }
    
    @Override
    public Course createCourse(String name, RouteData routeData) {
        List<Waypoint> waypointList = new ArrayList<Waypoint>();
        for (ControlPoint controlPoint : routeData.getPoints()) {
            Waypoint waypoint = createWaypoint(controlPoint);
            waypointList.add(waypoint);
        }
        return new CourseImpl(name, waypointList);
    }

}
