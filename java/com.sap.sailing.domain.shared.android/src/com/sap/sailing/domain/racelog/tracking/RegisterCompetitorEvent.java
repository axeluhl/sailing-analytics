package com.sap.sailing.domain.racelog.tracking;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.IsManagedBySharedDomainFactory;
import com.sap.sailing.domain.racelog.RaceLogEvent;

/**
 * Register a competitor for that race using the {@link RaceLog} in racelog-tracked races.
 * This can also be done by adding a {@link DeviceCompetitorMappingEvent}.
 * 
 * A dummy {@link Competitor} implementation with only an {@link Competitor#getId() id} may be used,
 * if the competitor is known to already exist on the server, as it is
 * {@link IsManagedBySharedDomainFactory#resolve(com.sap.sailing.domain.base.SharedDomainFactory) resolved}
 * on arrival.
 * @author Fredrik Teschke
 *
 */
public interface RegisterCompetitorEvent extends RaceLogEvent {
    Competitor getCompetitor();
}
