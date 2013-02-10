package com.sap.sailing.domain.swisstimingadapter;

import java.util.List;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.RaceLogStore;
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

    Regatta getOrCreateDefaultRegatta(String raceID, TrackedRegattaRegistry trackedRegattaRegistry, RaceLogStore raceLogStore);

    Nationality getOrCreateNationality(String threeLetterIOCCode);

    Competitor getOrCreateCompetitor(com.sap.sailing.domain.swisstimingadapter.Competitor competitor, BoatClass boatClass);
    
    Competitor getOrCreateCompetitor(String boatID, String threeLetterIOCCode, String name, BoatClass boatClass);

    RaceDefinition createRaceDefinition(Regatta regatta, Race race, StartList startList, com.sap.sailing.domain.swisstimingadapter.Course course);

    com.sap.sailing.domain.base.Mark getOrCreateMark(String trackerID);
    
    GPSFixMoving createGPSFix(TimePoint timePointOfTransmission, Fix fix);

    Competitor getCompetitorByBoatIDAndBoatClass(String boatID, BoatClass boatClass);

    void updateCourseWaypoints(Course courseToUpdate, Iterable<Mark> marks) throws PatchFailedException;
    
    MarkPassing createMarkPassing(TimePoint timePoint, Waypoint waypoint, Competitor competitor);

    void removeRace(String raceID);
    
    RaceTrackingConnectivityParameters createTrackingConnectivityParameters(String hostname, int port, String raceID, 
            boolean canSendRequests, long delayToLiveInMillis,
            SwissTimingFactory swissTimingFactory, DomainFactory domainFactory, WindStore windStore,
            RaceSpecificMessageLoader messageLoader, RaceLogStore raceLogStore);

    BoatClass getOrCreateBoatClassFromRaceID(String raceID);

    ControlPoint getOrCreateControlPoint(Iterable<String> devices);

    RaceDefinition createRaceDefinition(Regatta regatta, String raceID, Iterable<Competitor> competitors,
            List<ControlPoint> courseDefinition);
}
