package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogRevokeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogTagEvent;

public class TagFinder extends RaceLogAnalyzer<List<RaceLogTagEvent>> {

    private final RaceLog raceLog;

    public TagFinder(RaceLog raceLog) {
        super(raceLog);
        this.raceLog = raceLog;
    }

    @Override
    protected List<RaceLogTagEvent> performAnalysis() {
        Iterable<RaceLogEvent> raceLogEvents = getAllEvents();
        List<RaceLogTagEvent> result = new ArrayList<>();
        for (RaceLogEvent raceLogEvent : raceLogEvents) {
            if (raceLogEvent instanceof RaceLogTagEvent) {
                // tag event is not revoked (at least until this point of time at scanning race log)
                RaceLogTagEvent tagEvent = (RaceLogTagEvent) raceLogEvent;
                if (!result.contains(tagEvent)) {
                    result.add(tagEvent);
                }
            } else if (raceLogEvent instanceof RaceLogRevokeEvent) {
                // tag event got revoked => update it in result list or add it to result as revoked tag event
                RaceLogRevokeEvent revokeEvent = (RaceLogRevokeEvent) raceLogEvent;
                RaceLogEvent revokedEvent = raceLog.getEventById(revokeEvent.getRevokedEventId());
                if (revokedEvent != null && revokedEvent instanceof RaceLogTagEvent) {
                    RaceLogTagEvent revokedTagEvent = (RaceLogTagEvent) revokedEvent;
                    int index = result.indexOf(revokedTagEvent);
                    if (index > 0) {
                        result.get(index).markAsRevoked(revokeEvent.getCreatedAt());
                    } else {
                        revokedTagEvent.markAsRevoked(revokeEvent.getCreatedAt());
                        result.add(revokedTagEvent);
                    }
                }
            }
        }
        return result;
    }
}
