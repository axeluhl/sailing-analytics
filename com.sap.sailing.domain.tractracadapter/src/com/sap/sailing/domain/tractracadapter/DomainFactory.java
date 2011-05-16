package com.sap.sailing.domain.tractracadapter;

import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.Person;
import com.sap.sailing.domain.base.Team;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tractracadapter.impl.DomainFactoryImpl;
import com.tractrac.clientmodule.Competitor;
import com.tractrac.clientmodule.ControlPoint;
import com.tractrac.clientmodule.data.Position;
import com.tractrac.clientmodule.data.RouteData;

public interface DomainFactory {
    static DomainFactory INSTANCE = new DomainFactoryImpl();
    
    com.sap.sailing.domain.base.Position createPosition(Position position);
    com.sap.sailing.domain.base.TimePoint createTimePoint(long timestamp);
    com.sap.sailing.domain.base.Waypoint createWaypoint(ControlPoint controlPoint);
    Course createCourse(String name, RouteData routeData);
    com.sap.sailing.domain.base.Competitor getCompetitor(Competitor competitor);
    Nationality getNationality(String nationalityName);
    GPSFixMoving createGPSFixMoving(Position position);
    Person getPerson(String name, Nationality nationality);
    Team getTeam(String name, Nationality nationality);
}
