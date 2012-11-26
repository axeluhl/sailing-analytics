package com.sap.sailing.domain.swisstimingadapter;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.swisstimingadapter.impl.DomainFactoryImpl;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.RaceTrackingConnectivityParameters;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;
import com.sap.sailing.domain.tracking.WindStore;

import difflib.PatchFailedException;

public interface DomainFactory {
    final static DomainFactory INSTANCE = new DomainFactoryImpl(com.sap.sailing.domain.base.DomainFactory.INSTANCE);
    
    com.sap.sailing.domain.base.DomainFactory getBaseDomainFactory();

    Regatta getOrCreateRegatta(String raceID, TrackedRegattaRegistry trackedRegattaRegistry);

    Nationality getOrCreateNationality(String nationalityName);

    Competitor getOrCreateCompetitor(com.sap.sailing.domain.swisstimingadapter.Competitor competitor, BoatClass boatClass);

    RaceDefinition createRaceDefinition(Regatta regatta, Race race, StartList startList, com.sap.sailing.domain.swisstimingadapter.Course course);

    com.sap.sailing.domain.base.Mark getOrCreateMark(String trackerID);
    
    GPSFixMoving createGPSFix(TimePoint timePointOfTransmission, Fix fix);

    Competitor getCompetitorByBoatID(String boatID);

    void updateCourseWaypoints(Course courseToUpdate, Iterable<Mark> marks) throws PatchFailedException;
    
    MarkPassing createMarkPassing(TimePoint timePoint, Waypoint waypoint, Competitor competitor);

    void removeRace(String raceID);
    
    RaceTrackingConnectivityParameters createTrackingConnectivityParameters(String hostname, int port, String raceID, 
            boolean canSendRequests, long delayToLiveInMillis,
            SwissTimingFactory swissTimingFactory, DomainFactory domainFactory, WindStore windStore,
            RaceSpecificMessageLoader messageLoader);
}
