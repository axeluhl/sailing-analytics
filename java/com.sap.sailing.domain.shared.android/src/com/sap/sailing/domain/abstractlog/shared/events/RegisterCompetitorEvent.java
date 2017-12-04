package com.sap.sailing.domain.abstractlog.shared.events;

import com.sap.sailing.domain.abstractlog.AbstractLogEvent;
import com.sap.sailing.domain.abstractlog.Revokable;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.common.IsManagedByCache;

/**
 * Register a competitor for {@link RaceLog} and {@link RegattaLog} tracked races and regattas.
 * 
 * A dummy {@link Competitor} implementation with only an {@link Competitor#getId() id} may be used,
 * if the competitor is known to already exist on the server, as it is
 * {@link IsManagedByCache#resolve(com.sap.sailing.domain.base.SharedDomainFactory) resolved}
 * on arrival.
 * @author Fredrik Teschke
 *
 */
public interface RegisterCompetitorEvent<VisitorT> extends AbstractLogEvent<VisitorT>, Revokable {
    Competitor getCompetitor();
}
