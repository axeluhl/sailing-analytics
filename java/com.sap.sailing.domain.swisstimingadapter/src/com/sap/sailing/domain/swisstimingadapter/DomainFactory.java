package com.sap.sailing.domain.swisstimingadapter;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.swisstimingadapter.impl.DomainFactoryImpl;

public interface DomainFactory {
    final static DomainFactory INSTANCE = new DomainFactoryImpl();
    
    Event getOrCreateEvent(String raceID);

    Nationality getOrCreateNationality(String nationalityName);

    Competitor getOrCreateCompetitor(com.sap.sailing.domain.swisstimingadapter.Competitor competitor);

    RaceDefinition createRaceDefinition(Event event, Race race, Course course);
}
