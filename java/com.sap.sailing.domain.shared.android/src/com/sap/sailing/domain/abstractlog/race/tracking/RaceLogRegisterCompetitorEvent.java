package com.sap.sailing.domain.abstractlog.race.tracking;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.shared.events.RegisterCompetitorEvent;

/**
 * ATTENTION: This is the old legacy race log event for a competitor registration from the time before bug2822 
 * DON'T delete or rename for backward compatibility
 */
public interface RaceLogRegisterCompetitorEvent extends RaceLogEvent, RegisterCompetitorEvent<RaceLogEventVisitor> {

}
