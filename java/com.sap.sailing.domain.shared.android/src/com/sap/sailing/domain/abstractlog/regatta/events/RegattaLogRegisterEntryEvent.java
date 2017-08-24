package com.sap.sailing.domain.abstractlog.regatta.events;

import com.sap.sailing.domain.abstractlog.Revokable;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.base.Competitor;

/**
 * Registers a single entry for the regatta, the entry can be is used as a competitor for all races or the
 * regatta or only for single races 
 * @author Frank Mittag
 *
 */
public interface RegattaLogRegisterEntryEvent extends RegattaLogEvent, Revokable {
    Competitor getCompetitor();
}
