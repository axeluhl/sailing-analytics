package com.sap.sailing.domain.racelog.tracking;

import com.sap.sailing.domain.base.Competitor;

/**
 * Also registers the competitor for the race, even if no {@link RegisterCompetitorEvent} is present.
 * @author Fredrik Teschke
 *
 */
public interface DeviceCompetitorMappingEvent extends DeviceMappingEvent<Competitor> {

}
