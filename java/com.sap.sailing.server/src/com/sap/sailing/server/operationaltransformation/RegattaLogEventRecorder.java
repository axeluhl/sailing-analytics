package com.sap.sailing.server.operationaltransformation;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.base.Regatta;

public class RegattaLogEventRecorder implements Serializable {
    private static final long serialVersionUID = -7097222052117730898L;
    private final RegattaLogEvent event;

    public RegattaLogEventRecorder(RegattaLogEvent event) {
        this.event = event;
    }

    private RegattaLogEvent getEvent() {
        return event;
    }

    /**
     * Adds the stored event to the regatta's regattalog.
     * 
     * @param regatta
     *            to add to.
     * @return <code>true</code> if add was successful.
     */
    RegattaLogEvent addEventTo(Regatta regatta) {
        if (regatta != null) {
            RegattaLog regattaLog = regatta.getRegattaLog();
            regattaLog.add(getEvent());
        }
        return getEvent();
    }
}
