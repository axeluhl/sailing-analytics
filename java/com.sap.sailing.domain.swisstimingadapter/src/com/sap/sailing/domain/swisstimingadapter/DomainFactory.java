package com.sap.sailing.domain.swisstimingadapter;

import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.swisstimingadapter.impl.DomainFactoryImpl;
import com.sap.sailing.domain.tracking.GPSFixMoving;

public interface DomainFactory {
    final static DomainFactory INSTANCE = new DomainFactoryImpl();
    
    Event getOrCreateEvent(String raceID);

    Nationality getOrCreateNationality(String nationalityName);

    Competitor getOrCreateCompetitor(com.sap.sailing.domain.swisstimingadapter.Competitor competitor);

    RaceDefinition createRaceDefinition(Event event, Race race, StartList startList, Course course);

    Buoy getOrCreateBuoy(String trackerID);
    
    GPSFixMoving createGPSFix(TimePoint timePointOfTransmission, Fix fix);

    Competitor getCompetitorByBoatID(String sailNumber);
}
