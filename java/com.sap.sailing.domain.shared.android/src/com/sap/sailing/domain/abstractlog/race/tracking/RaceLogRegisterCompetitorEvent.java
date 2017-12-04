package com.sap.sailing.domain.abstractlog.race.tracking;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.shared.events.RegisterCompetitorEvent;

/**
 * The event registers a {@link Competitor} or {@link CompetitorWithBoat} on a race.
 * The competitor type is determined by the {@link Regatta} attribute 'canBoatsOfCompetitorsChangePerRace'
 * If 'canBoatsOfCompetitorsChangePerRace' is true the type must be {@link Competitor}    
 * If 'canBoatsOfCompetitorsChangePerRace' is false the type must be {@link CompetitorWithBoat}    
 */
public interface RaceLogRegisterCompetitorEvent extends RaceLogEvent, RegisterCompetitorEvent<RaceLogEventVisitor> {

}
