package com.sap.sailing.racecommittee.app.domain.state.impl.analyzers;

import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.racelog.RaceLogRaceStatusEvent;
import com.sap.sailing.racecommittee.app.logging.ExLog;

public class RaceStatusAnalyzer extends RaceLogAnalyzer {
    private static final String TAG = RaceStatusAnalyzer.class.getName();

    public RaceStatusAnalyzer(RaceLog raceLog) {
        super(raceLog);
    }

    public RaceLogRaceStatus getStatus() {
        ExLog.i(TAG, String.format("Updating status..."));

        RaceLogRaceStatus newStatus = RaceLogRaceStatus.UNSCHEDULED;
        for (RaceLogEvent event : getPassEvents()) {
            ExLog.i(TAG, String.format("Deciding on event of type %s.", event.getClass().getSimpleName()));
            if (event instanceof RaceLogRaceStatusEvent) {
                RaceLogRaceStatusEvent statusEvent = (RaceLogRaceStatusEvent) event;
                ExLog.i(TAG, String.format("Decision to %s.", statusEvent.getNextStatus()));
                newStatus = statusEvent.getNextStatus();
            }
        }

        ExLog.i(TAG, String.format("Status will be set to %s.", newStatus));
        return newStatus;
    }

}
