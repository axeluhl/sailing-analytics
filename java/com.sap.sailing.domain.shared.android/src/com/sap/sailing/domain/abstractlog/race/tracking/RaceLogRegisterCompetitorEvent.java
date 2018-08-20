package com.sap.sailing.domain.abstractlog.race.tracking;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.shared.events.RegisterCompetitorEvent;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorWithBoat;

/**
 * The event registers a {@link CompetitorWithBoat} or a pair of {@link Competitor} and {@link Boat} on a race.
 * The competitor type is determined by the {@link Regatta} attribute 'canBoatsOfCompetitorsChangePerRace'
 * If 'canBoatsOfCompetitorsChangePerRace' is true the type must be {@link Competitor}    
 * If 'canBoatsOfCompetitorsChangePerRace' is false the type must be {@link CompetitorWithBoat}    
 */
public interface RaceLogRegisterCompetitorEvent extends RaceLogEvent, RegisterCompetitorEvent<RaceLogEventVisitor> {
    Boat getBoat();
}
