package com.sap.sailing.domain.abstractlog.regatta.events;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.shared.events.RegisterCompetitorEvent;

/**
 * ATTENTION: This is the old legacy regatta log event for a competitor registration from the time before bug2822 
 * DON'T delete or rename for backward compatibility
 */
public interface RegattaLogRegisterCompetitorEvent
        extends RegattaLogEvent, RegisterCompetitorEvent<RegattaLogEventVisitor> {

}
