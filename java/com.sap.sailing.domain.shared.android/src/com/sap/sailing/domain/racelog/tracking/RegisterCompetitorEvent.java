package com.sap.sailing.domain.racelog.tracking;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.IsManagedByCache;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.Revokable;

/**
 * Register a competitor for that race using the {@link RaceLog} in racelog-tracked races.
 * 
 * A dummy {@link Competitor} implementation with only an {@link Competitor#getId() id} may be used,
 * if the competitor is known to already exist on the server, as it is
 * {@link IsManagedByCache#resolve(com.sap.sailing.domain.base.SharedDomainFactory) resolved}
 * on arrival.
 * @author Fredrik Teschke
 *
 */
public interface RegisterCompetitorEvent extends RaceLogEvent, Revokable {
    Competitor getCompetitor();
}
