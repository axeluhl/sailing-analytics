package com.sap.sailing.domain.abstractlog.race.tracking;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.Revokable;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.IsManagedBySharedDomainFactory;

/**
 * Register a competitor for that race using the {@link RaceLog} in racelog-tracked races.
 * 
 * A dummy {@link Competitor} implementation with only an {@link Competitor#getId() id} may be used,
 * if the competitor is known to already exist on the server, as it is
 * {@link IsManagedBySharedDomainFactory#resolve(com.sap.sailing.domain.base.SharedDomainFactory) resolved}
 * on arrival.
 * @author Fredrik Teschke
 *
 */
public interface RegisterCompetitorEvent extends RaceLogEvent, Revokable {
    Competitor getCompetitor();
}
