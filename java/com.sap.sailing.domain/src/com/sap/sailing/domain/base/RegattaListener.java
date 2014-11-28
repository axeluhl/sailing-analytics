package com.sap.sailing.domain.base;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;

public interface RegattaListener {
    void raceAdded(Regatta regatta, RaceDefinition race);
    void raceRemoved(Regatta regatta, RaceDefinition race);
    void regattaLogEventAdded(final Regatta regatta, final RegattaLogEvent event); 
}
