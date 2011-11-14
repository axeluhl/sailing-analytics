package com.sap.sailing.domain.swisstimingadapter;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.swisstimingadapter.impl.DomainFactoryImpl;

public interface DomainFactory {
    final static DomainFactory INSTANCE = new DomainFactoryImpl();
    
    Event getOrCreateEvent(Race race);

    Nationality getOrCreateNationality(String nationalityName);

    Competitor getOrCreateCompetitor(com.sap.sailing.domain.swisstimingadapter.Competitor competitor);
}
