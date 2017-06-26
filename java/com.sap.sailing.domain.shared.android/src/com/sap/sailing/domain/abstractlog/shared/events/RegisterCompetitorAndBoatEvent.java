package com.sap.sailing.domain.abstractlog.shared.events;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.common.IsManagedByCache;

/**
 * Register a competitor together with a boat for {@link RaceLog} and {@link RegattaLog} trackeds race and regattas.
 * 
 * A dummy {@link Competitor} implementation with only an {@link Competitor#getId() id} may be used,
 * if the competitor is known to already exist on the server, as it is
 * {@link IsManagedByCache#resolve(com.sap.sailing.domain.base.SharedDomainFactory) resolved}
 * on arrival.
 * @author Frank Mittag
 *
 */
public interface RegisterCompetitorAndBoatEvent<VisitorT> extends RegisterBoatEvent<VisitorT>, RegisterCompetitorEvent<VisitorT> {
}
