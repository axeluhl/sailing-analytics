package com.sap.sailing.server.operationaltransformation;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;

public class RaceLogEventRecorder implements Serializable {
    private static final long serialVersionUID = -5426128599743917274L;
    private final String fleetName;
    private final RaceLogEvent event;

    public RaceLogEventRecorder(String fleetName, RaceLogEvent event) {
        this.fleetName = fleetName;
        this.event = event;
    }

    private String getFleetName() {
        return fleetName;
    }

    private RaceLogEvent getEvent() {
        return event;
    }

    /**
     * Adds the stored event to the race column's race log.
     * 
     * @param raceColumn
     *            to add to.
     * @param fleetName
     *            to resolve correct {@link RaceLog}.
     * @return <code>true</code> if add was successful.
     */
    RaceLogEvent addEventTo(RaceColumn raceColumn) {
        if (raceColumn != null) {
            Fleet fleet = raceColumn.getFleetByName(getFleetName());
            RaceLog raceLog = raceColumn.getRaceLog(fleet);
            raceLog.add(getEvent());
        }
        return getEvent();
    }
}
