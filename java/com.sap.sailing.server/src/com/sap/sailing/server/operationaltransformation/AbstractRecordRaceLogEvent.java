package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;

public abstract class AbstractRecordRaceLogEvent extends AbstractRacingEventServiceOperation<RaceLogEvent> {
    private static final long serialVersionUID = 2140858355670664173L;

    protected final String raceColumnName;
    private final String fleetName;
    private final RaceLogEvent event;

    public AbstractRecordRaceLogEvent(String raceColumnName, String fleetName, RaceLogEvent event) {
        this.raceColumnName = raceColumnName;
        this.fleetName = fleetName;
        this.event = event;
    }

    /**
     * Adds the stored event to the race column's race log.
     * @param raceColumn to add to.
     * @param fleetName to resolve correct {@link RaceLog}.
     * @return <code>true</code> if add was successful.
     */
    protected RaceLogEvent addEventTo(RaceColumn raceColumn) {
        Fleet fleet = raceColumn.getFleetByName(fleetName);
        RaceLog raceLog = raceColumn.getRaceLog(fleet);
        raceLog.add(event);
        return event;
    }

}
