package com.sap.sailing.domain.abstractlog.regatta.events;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.shared.events.RegisterCompetitorAndBoatEvent;

/**
 * Registers a competitor together with a boat for {@link RegattaLog} tracked regattas.
 * @author Frank Mittag
 *
 */
public interface RegattaLogRegisterCompetitorAndBoatEvent
        extends RegattaLogEvent, RegisterCompetitorAndBoatEvent<RegattaLogEventVisitor> {

}
