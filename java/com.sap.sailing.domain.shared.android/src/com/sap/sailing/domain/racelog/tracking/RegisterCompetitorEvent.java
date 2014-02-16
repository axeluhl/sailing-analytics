package com.sap.sailing.domain.racelog.tracking;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.racelog.RaceLogEvent;

/**
 * Register a competitor for that race using the {@link RaceLog} in racelog-tracked races.
 * This can also be done by simply adding a {@link DeviceCompetitorMappingEvent}.
 * @author Fredrik Teschke
 *
 */
public interface RegisterCompetitorEvent extends RaceLogEvent {
	Competitor getCompetitor();
}
