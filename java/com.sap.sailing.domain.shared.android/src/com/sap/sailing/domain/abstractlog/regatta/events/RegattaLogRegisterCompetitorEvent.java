package com.sap.sailing.domain.abstractlog.regatta.events;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.shared.events.RegisterCompetitorEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorWithBoat;

/**
* The event registers a {@link Competitor} or {@link CompetitorWithBoat} on a regatta.
* The competitor type is determined by the {@link Regatta} attribute 'canBoatsOfCompetitorsChangePerRace'
* If 'canBoatsOfCompetitorsChangePerRace' is true the type must be {@link Competitor}    
* If 'canBoatsOfCompetitorsChangePerRace' is false the type must be {@link CompetitorWithBoat}    
*/
public interface RegattaLogRegisterCompetitorEvent
        extends RegattaLogEvent, RegisterCompetitorEvent<RegattaLogEventVisitor> {

}
